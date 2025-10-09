package com.project.maumii_be.service.record;

import com.google.cloud.storage.*;
import com.project.maumii_be.domain.*;
import com.project.maumii_be.domain.Record;
import com.project.maumii_be.dto.bubble.BubbleReq;
import com.project.maumii_be.dto.record.CreateRecordReq;
import com.project.maumii_be.dto.record.RecordReq;
import com.project.maumii_be.dto.record.RecordRes;
import com.project.maumii_be.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecordSaveService {

    private final RecordRepository recordRepository;
    private final RecordListRepository recordListRepository;
    private final UserRepository userRepository;

    @Value("${storage.bucket}")
    private String bucketName;

    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    private static final String GCS_PREFIX = "voices/";

    /** ffmpeg 존재 여부 1회 체크 */
    private volatile boolean ffmpegChecked = false;

    public Long saveRecordWithBubbles(CreateRecordReq payload,
                                      MultiValueMap<String, MultipartFile> files,
                                      String uId) {
        final String reqId = UUID.randomUUID().toString().substring(0,8); // 간단 상관관계 ID
        final long t0 = System.nanoTime();

        try {
            // --- 요청 요약 로그
            log.info("[rec-save {}] START user={} rlId={} title={} bubbles={} fileKeys={}",
                    reqId,
                    uId,
                    payload.record() != null ? payload.record().getRlId() : null,
                    payload.recordListTitle(),
                    payload.bubbles() == null ? 0 : payload.bubbles().size(),
                    files == null ? "[]" : files.keySet());

            // 필수값 검증
            RecordReq rreq = Objects.requireNonNull(payload.record(), "record is required");

            var owner = userRepository.findById(uId)
                    .orElseThrow(() -> new IllegalArgumentException("user not found: " + uId));

            RecordList recordList = resolveOrCreateRecordList(rreq.getRlId(), payload.recordListTitle(), owner);

            Record rec = rreq.toRecord(rreq);
            rec.setRecordList(recordList);
            if (rec.getBubbles() == null) rec.setBubbles(new ArrayList<>());

            // ffmpeg 점검(최초 1회)
            checkFfmpegOnce(reqId);

            List<Path> bubbleWavs = new ArrayList<>();
            long totalMs = 0L;

            // --- 버블별 파일/길이 처리
            if (payload.bubbles() != null) {
                for (BubbleReq bq : payload.bubbles()) {
                    MultipartFile f = (bq.getFileField() == null ? null : files.getFirst(bq.getFileField()));
                    if (f != null) {
                        log.info("[rec-save {}] bubble field='{}' size={} contentType={}",
                                reqId, bq.getFileField(), f.getSize(), f.getContentType());
                    }

                    if (f != null && !f.isEmpty()) {
                        Path wav = transcodeToWav(f, reqId);
                        bubbleWavs.add(wav);
                    }

                    Bubble b = bq.toBubble(bq);
                    if (b.getBTalker() == null || b.getBTalker().isBlank()) b.setBTalker("partner");
                    if (b.getBLength() == null && bq.getDurationMs() != null) {
                        b.setBLength(msToLocalTime(bq.getDurationMs()));
                    }
                    rec.getBubbles().add(b);
                    b.setRecord(rec);
                    if (bq.getDurationMs() != null) totalMs += Math.max(0, bq.getDurationMs());
                }
            }

            // --- 병합 & 업로드
            if (!bubbleWavs.isEmpty()) {
                Path merged = mergeWavFiles(bubbleWavs, reqId);
                long bytes = Files.size(merged);
                String objectName = GCS_PREFIX + UUID.randomUUID() + ".wav";

                log.info("[rec-save {}] uploading to GCS bucket={} object={} bytes={}",
                        reqId, bucketName, objectName, bytes);

                BlobInfo info = BlobInfo.newBuilder(bucketName, objectName)
                        .setContentType("audio/wav")
                        .build();
                storage.create(info, Files.readAllBytes(merged));

                rec.setRVoice(objectName);
                Files.deleteIfExists(merged);

                // 서명 URL(짧게만 로그)
                URL signedUrl = storage.signUrl(info, 5, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
                log.info("[rec-save {}] GCS uploaded ok url(sample)={}...", reqId, trimUrl(signedUrl, 80));
            } else {
                log.info("[rec-save {}] no audio parts to upload (bubbles without files)", reqId);
            }

            if (totalMs > 0) rec.setRLength(msToLocalTime(totalMs));
            recordRepository.save(rec);

            long ms = (System.nanoTime() - t0) / 1_000_000;
            log.info("[rec-save {}] DONE recordId={} in {} ms", reqId, rec.getRId(), ms);
            return rec.getRId();

        } catch (Exception e) {
            long ms = (System.nanoTime() - t0) / 1_000_000;
            log.error("[rec-save {}] FAIL after {} ms : {}",
                    reqId, ms, e.toString(), e);
            // 프런트가 500만 보는 것보다 원인 전달
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "record save failed: " + e.getMessage(), e);
        }
    }

    // ---------- ffmpeg 유틸 ----------

    private void checkFfmpegOnce(String reqId) throws IOException, InterruptedException {
        if (ffmpegChecked) return;
        synchronized (this) {
            if (ffmpegChecked) return;
            CmdResult r = runCmd(true, "ffmpeg", "-version");
            log.info("[rec-save {}] ffmpeg -version exit={} head={}",
                    reqId, r.exit, head(r.stderr + r.stdout, 200));
            ffmpegChecked = true;
        }
    }

    private Path transcodeToWav(MultipartFile file, String reqId) throws IOException, InterruptedException {
        Path temp = Files.createTempFile("raw_", ".bin");
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
        }
        Path dst = Files.createTempFile("out_", ".wav");

        CmdResult r = runCmd(true,
                "ffmpeg", "-y",
                "-i", temp.toAbsolutePath().toString(),
                "-vn", "-acodec", "pcm_s16le",
                "-ar", "16000", "-ac", "1",
                dst.toAbsolutePath().toString()
        );
        log.info("[rec-save {}] transcode exit={} outHead={}", reqId, r.exit, head(r.stderr, 300));

        Files.deleteIfExists(temp);
        if (r.exit != 0 || !Files.exists(dst) || Files.size(dst) == 0) {
            throw new IllegalStateException("ffmpeg transcode failed, exit=" + r.exit + " err=" + head(r.stderr, 500));
        }
        return dst;
    }

    private Path mergeWavFiles(List<Path> wavs, String reqId) throws IOException, InterruptedException {
        Path listFile = Files.createTempFile("concat_", ".txt");
        StringBuilder sb = new StringBuilder();
        for (Path p : wavs) sb.append("file '").append(p.toAbsolutePath()).append("'\n");
        Files.writeString(listFile, sb.toString());

        Path out = Files.createTempFile("merged_", ".wav");

        CmdResult r = runCmd(true,
                "ffmpeg", "-y",
                "-safe", "0",
                "-f", "concat",
                "-i", listFile.toAbsolutePath().toString(),
                "-acodec", "pcm_s16le", "-ar", "16000", "-ac", "1",
                out.toAbsolutePath().toString()
        );
        Files.deleteIfExists(listFile);
        log.info("[rec-save {}] merge exit={} outHead={}", reqId, r.exit, head(r.stderr, 300));

        if (r.exit != 0 || !Files.exists(out) || Files.size(out) == 0) {
            throw new IllegalStateException("ffmpeg concat failed, exit=" + r.exit + " err=" + head(r.stderr, 500));
        }
        // 임시 wav 삭제
        for (Path p : wavs) Files.deleteIfExists(p);
        return out;
    }

    private static class CmdResult {
        int exit;
        String stdout;
        String stderr;
    }

    /** stderr/stdout 캡처해서 로그에 남김 (redirectErrorStream 사용 안 함) */
    private CmdResult runCmd(boolean inheritEnv, String... cmd) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        if (inheritEnv) pb.environment().put("PATH", System.getenv("PATH"));
        pb.redirectErrorStream(false);
        Process p = pb.start();

        String stdout = readAll(p.getInputStream());
        String stderr = readAll(p.getErrorStream());
        int exit = p.waitFor();

        CmdResult r = new CmdResult();
        r.exit = exit; r.stdout = stdout; r.stderr = stderr;
        return r;
    }

    private static String readAll(InputStream in) throws IOException {
        try (in) { return new String(in.readAllBytes(), StandardCharsets.UTF_8); }
    }

    private static String head(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + " ...";
    }

    private static String trimUrl(URL url, int max) {
        String s = String.valueOf(url);
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    // ---------- 기존 유틸/조회 ----------

    private RecordList resolveOrCreateRecordList(Long rlId, String newTitle, User owner) {
        if (rlId != null)
            return recordListRepository.findById(rlId)
                    .orElseThrow(() -> new IllegalArgumentException("recordList not found"));
        if (newTitle != null && !newTitle.isBlank()) {
            RecordList rl = new RecordList();
            rl.setUser(owner);
            rl.setRlName(newTitle);
            return recordListRepository.save(rl);
        }
        RecordList rl = new RecordList();
        rl.setRlName("기본");
        rl.setUser(owner);
        return recordListRepository.save(rl);
    }

    private java.time.LocalTime msToLocalTime(Long ms) {
        long sec = Math.max(0, ms / 1000);
        int nano = (int) ((ms % 1000) * 1_000_000);
        return java.time.LocalTime.ofSecondOfDay(sec).withNano(nano);
    }

    public List<RecordRes> getRecordsInList(Long rlId, String userIdOrNull) {
        List<Record> records = recordRepository.findAllWithBubblesByRecordListId(rlId);
        return records.stream().map(RecordRes::toRecordRes).toList();
    }
}