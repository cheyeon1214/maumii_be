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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
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

    // ✅ GCS 설정
    @Value("${storage.bucket}")
    private String bucketName;

    private final Storage storage = StorageOptions.getDefaultInstance().getService();

    // GCS에 저장할 경로 prefix
    private static final String GCS_PREFIX = "voices/";

    // ---------------------- 핵심 변경: GCS에 저장 ----------------------
    public Long saveRecordWithBubbles(CreateRecordReq payload,
                                      MultiValueMap<String, MultipartFile> files,
                                      String uId) throws IOException, InterruptedException {

        RecordReq rreq = payload.record();
        if (rreq == null) throw new IllegalArgumentException("record is required");

        var owner = userRepository.findById(uId)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + uId));

        RecordList recordList = resolveOrCreateRecordList(rreq.getRlId(), payload.recordListTitle(), owner);

        Record rec = rreq.toRecord(rreq);
        rec.setRecordList(recordList);
        if (rec.getBubbles() == null) rec.setBubbles(new ArrayList<>());

        List<Path> bubbleWavs = new ArrayList<>();
        long totalMs = 0L;

        // (1) 개별 버블 파일 처리
        if (payload.bubbles() != null) {
            for (BubbleReq bq : payload.bubbles()) {
                MultipartFile f = (bq.getFileField() == null ? null : files.getFirst(bq.getFileField()));
                if (f != null && !f.isEmpty()) {
                    Path wav = transcodeToWav(f);
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

        // (2) 버블 WAV들 병합 → GCS 업로드
        if (!bubbleWavs.isEmpty()) {
            Path merged = mergeWavFiles(bubbleWavs);
            byte[] data = Files.readAllBytes(merged);
            String objectName = GCS_PREFIX + UUID.randomUUID() + ".wav";

            BlobInfo info = BlobInfo.newBuilder(bucketName, objectName)
                    .setContentType("audio/wav")
                    .build();
            storage.create(info, data);

            rec.setRVoice(objectName);
            Files.deleteIfExists(merged);

            // 서명 URL(15분 유효)
            URL signedUrl = storage.signUrl(info, 15, TimeUnit.MINUTES,
                    Storage.SignUrlOption.withV4Signature());
            log.info("GCS uploaded: {}", signedUrl);
        }

        if (totalMs > 0) rec.setRLength(msToLocalTime(totalMs));
        recordRepository.save(rec);
        return rec.getRId();
    }

    // WAV 변환
    private Path transcodeToWav(MultipartFile file) throws IOException, InterruptedException {
        Path temp = Files.createTempFile("raw_", ".bin");
        Files.copy(file.getInputStream(), temp, StandardCopyOption.REPLACE_EXISTING);

        Path dst = Files.createTempFile("out_", ".wav");
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y",
                "-i", temp.toAbsolutePath().toString(),
                "-vn", "-acodec", "pcm_s16le",
                "-ar", "16000", "-ac", "1",
                dst.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();
        p.getInputStream().transferTo(OutputStream.nullOutputStream());
        int exit = p.waitFor();
        Files.deleteIfExists(temp);
        if (exit != 0) throw new RuntimeException("ffmpeg transcode failed, exit=" + exit);
        return dst;
    }

    // WAV 병합
    private Path mergeWavFiles(List<Path> wavs) throws IOException, InterruptedException {
        Path listFile = Files.createTempFile("concat_", ".txt");
        StringBuilder sb = new StringBuilder();
        for (Path p : wavs)
            sb.append("file '").append(p.toAbsolutePath()).append("'\n");
        Files.writeString(listFile, sb.toString());

        Path out = Files.createTempFile("merged_", ".wav");
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y",
                "-safe", "0",
                "-f", "concat",
                "-i", listFile.toAbsolutePath().toString(),
                "-acodec", "pcm_s16le", "-ar", "16000", "-ac", "1",
                out.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();
        p.getInputStream().transferTo(OutputStream.nullOutputStream());
        int exit = p.waitFor();
        Files.deleteIfExists(listFile);
        if (exit != 0) throw new RuntimeException("ffmpeg concat failed");
        return out;
    }

    // 유틸들
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