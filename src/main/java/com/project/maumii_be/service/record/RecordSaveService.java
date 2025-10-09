// src/main/java/com/project/maumii_be/service/record/RecordSaveService.java
package com.project.maumii_be.service.record;

import com.google.cloud.storage.*;
import com.project.maumii_be.domain.Bubble;
import com.project.maumii_be.domain.Record;
import com.project.maumii_be.domain.RecordList;
import com.project.maumii_be.domain.User;
import com.project.maumii_be.dto.bubble.BubbleReq;
import com.project.maumii_be.dto.record.CreateRecordReq;
import com.project.maumii_be.dto.record.RecordReq;
import com.project.maumii_be.dto.record.RecordRes;
import com.project.maumii_be.repository.RecordListRepository;
import com.project.maumii_be.repository.RecordRepository;
import com.project.maumii_be.repository.UserRepository;
import com.project.maumii_be.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class RecordSaveService {

    private final RecordRepository recordRepository;
    private final RecordListRepository recordListRepository;
    private final UserRepository userRepository;

    // GCS
    @Value("${storage.bucket}")
    private String bucketName;
    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    private static final String GCS_PREFIX = "voices/"; // GCS 오브젝트 경로 prefix

    // 암호화 키 (Bean 주입)
    private final SecretKey secretKey;

    // ffmpeg 존재 확인(1회)
    private volatile boolean ffmpegChecked = false;

    public Long saveRecordWithBubbles(CreateRecordReq payload,
                                      MultiValueMap<String, MultipartFile> files,
                                      String uId) {
        try {
            RecordReq rreq = Objects.requireNonNull(payload.record(), "record is required");

            User owner = userRepository.findById(uId)
                    .orElseThrow(() -> new IllegalArgumentException("user not found: " + uId));

            RecordList recordList = resolveOrCreateRecordList(rreq.getRlId(), payload.recordListTitle(), owner);

            Record rec = rreq.toRecord(rreq);
            rec.setRecordList(recordList);
            if (rec.getBubbles() == null) rec.setBubbles(new ArrayList<>());

            checkFfmpegOnce();

            List<Path> bubbleWavs = new ArrayList<>();
            long totalMs = 0L;

            if (payload.bubbles() != null) {
                for (BubbleReq bq : payload.bubbles()) {
                    MultipartFile f = (bq.getFileField() == null ? null : files.getFirst(bq.getFileField()));
                    if (f != null && !f.isEmpty()) {
                        Path wav = transcodeToWav(f); // 16kHz/mono/pcm_s16le
                        bubbleWavs.add(wav);
                    }

                    Bubble b = bq.toBubble(bq);
                    if (b.getBTalker() == null || b.getBTalker().isBlank()) b.setBTalker("partner");
                    if (b.getBLength() == null && bq.getDurationMs() != null) {
                        b.setBLength(msToLocalTime(bq.getDurationMs()));
                        totalMs += Math.max(0, bq.getDurationMs());
                    }
                    rec.getBubbles().add(b);
                    b.setRecord(rec);
                }
            }

            // 버블들을 하나로 병합 → 암호화(.enc) → GCS 업로드
            if (!bubbleWavs.isEmpty()) {
                Path mergedWav = mergeWavFiles(bubbleWavs);          // 병합 WAV
                Path encrypted = encryptFile(mergedWav);             // .enc 생성
                String objectName = GCS_PREFIX + UUID.randomUUID() + ".enc";
                uploadToGcs(objectName, encrypted, "application/octet-stream");

                // 정리
                Files.deleteIfExists(mergedWav);
                Files.deleteIfExists(encrypted);
                for (Path p : bubbleWavs) Files.deleteIfExists(p);

                // DB에는 GCS 오브젝트 경로 저장 (예: voices/xxxx.enc)
                rec.setRVoice(objectName);
            }

            if (totalMs > 0) rec.setRLength(msToLocalTime(totalMs));
            recordRepository.save(rec);
            return rec.getRId();

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "record save failed: " + e.getMessage(), e);
        }
    }

    /* ---------------- ffmpeg & 파일 유틸 ---------------- */

    private void checkFfmpegOnce() throws IOException, InterruptedException {
        if (ffmpegChecked) return;
        synchronized (this) {
            if (ffmpegChecked) return;
            runFfmpeg("-version"); // 예외 발생 시 상위에서 잡힘
            ffmpegChecked = true;
        }
    }

    private Path transcodeToWav(MultipartFile file) throws IOException, InterruptedException {
        Path temp = Files.createTempFile("raw_", ".bin");
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
        }
        Path dst = Files.createTempFile("out_", ".wav");

        runFfmpeg(
                "-y",
                "-i", temp.toAbsolutePath().toString(),
                "-vn", "-acodec", "pcm_s16le",
                "-ar", "16000", "-ac", "1",
                dst.toAbsolutePath().toString()
        );
        Files.deleteIfExists(temp);

        if (!Files.exists(dst) || Files.size(dst) == 0)
            throw new IllegalStateException("ffmpeg transcode produced empty file");
        return dst;
    }

    private Path mergeWavFiles(List<Path> wavs) throws IOException, InterruptedException {
        Path listFile = Files.createTempFile("concat_", ".txt");
        StringBuilder sb = new StringBuilder();
        for (Path p : wavs) {
            sb.append("file '")
                    .append(p.toAbsolutePath().toString().replace("'", "'\\''"))
                    .append("'\n");
        }
        Files.writeString(listFile, sb.toString());

        Path out = Files.createTempFile("merged_", ".wav");
        runFfmpeg(
                "-y",
                "-safe", "0",
                "-f", "concat",
                "-i", listFile.toAbsolutePath().toString(),
                "-acodec", "pcm_s16le", "-ar", "16000", "-ac", "1",
                out.toAbsolutePath().toString()
        );
        Files.deleteIfExists(listFile);

        if (!Files.exists(out) || Files.size(out) == 0)
            throw new IllegalStateException("ffmpeg concat produced empty file");
        return out;
    }

    private void runFfmpeg(String... args) throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        cmd.addAll(Arrays.asList(args));
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try (InputStream in = p.getInputStream()) {
            in.transferTo(OutputStream.nullOutputStream()); // 버퍼 소비
        }
        int exit = p.waitFor();
        if (exit != 0) throw new IllegalStateException("ffmpeg exit=" + exit);
    }

    /* --------------- 암호화 & 업로드 --------------- */

    private Path encryptFile(Path srcWav) {
        try {
            Path enc = Files.createTempFile("voice_", ".enc");
            EncryptionUtil.encryptFile(srcWav.toFile(), enc.toFile(), secretKey);
            return enc;
        } catch (Exception e) {
            throw new RuntimeException("encrypt failed", e);
        }
    }

    private void uploadToGcs(String objectName, Path file, String contentType) throws IOException {
        BlobInfo info = BlobInfo.newBuilder(bucketName, objectName)
                .setContentType(contentType)
                .build();
        storage.create(info, Files.readAllBytes(file));
    }

    /* --------------- 기존 유틸/조회 --------------- */

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
        return recordRepository.findAllWithBubblesByRecordListId(rlId)
                .stream().map(RecordRes::toRecordRes).toList();
    }
}