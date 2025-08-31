// src/main/java/com/project/maumii_be/service/record/RecordSaveService.java
package com.project.maumii_be.service.record;

import com.project.maumii_be.domain.Bubble;
import com.project.maumii_be.domain.Record;
import com.project.maumii_be.domain.RecordList;
import com.project.maumii_be.domain.User;
import com.project.maumii_be.dto.BubbleReq;
import com.project.maumii_be.dto.CreateRecordReq;
import com.project.maumii_be.dto.RecordReq;
import com.project.maumii_be.dto.RecordRes;
import com.project.maumii_be.repository.RecordListRepository;
import com.project.maumii_be.repository.RecordRepository;
import com.project.maumii_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecordSaveService {
    private final RecordRepository recordRepository;
    private final RecordListRepository recordListRepository;
    private final UserRepository userRepository;

    private final Path root = Paths.get(System.getProperty("app.upload.dir", "uploads/voices"));

    public Long saveRecordWithBubbles(CreateRecordReq payload,
                                      MultiValueMap<String, MultipartFile> files, String uId) {
        try { Files.createDirectories(root); } catch (IOException ignore) {}

        // --- RecordList resolve/create ---
        RecordReq rreq = payload.record();
        if (rreq == null) throw new IllegalArgumentException("record is required");
        var owner = userRepository.findById(uId)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + uId));


        RecordList recordList = resolveOrCreateRecordList(rreq.getRlId(), payload.recordListTitle(), owner);

        // --- Record entity ---
        Record rec = rreq.toRecord(rreq);
        rec.setRecordList(recordList);
//        rec.setUser(owner);
        if (rec.getBubbles() == null) rec.setBubbles(new ArrayList<>());

        // (선택) 세션 통짜 오디오가 프론트에서 따로 올 때만 사용
        if (payload.voiceField() != null) {
            MultipartFile vf = files.getFirst(payload.voiceField());
            if (vf != null && !vf.isEmpty()) {
                rec.setRVoice(saveVoice(vf));
            }
        }

        // --- 버블 저장 + 파일 경로 수집 ---
        List<Path> bubbleFiles = new ArrayList<>();
        long totalMs = 0L;

        if (payload.bubbles() != null && !payload.bubbles().isEmpty()) {
            for (BubbleReq bq : payload.bubbles()) {
                // 파일 저장
                String key = bq.getFileField();
                MultipartFile f = (key == null ? null : files.getFirst(key));
                if (f != null && !f.isEmpty()) {
                    Path saved = saveVoiceReturnPath(f);
                    bubbleFiles.add(saved);
                }

                // 엔티티 변환/보정
                Bubble b = bq.toBubble(bq);
                if (b.getBTalker() == null || b.getBTalker().isBlank()) b.setBTalker("partner");
                if (b.getBLength() == null && bq.getDurationMs() != null) {
                    b.setBLength(msToLocalTime(bq.getDurationMs()));
                }

                rec.getBubbles().add(b);
                b.setRecord(rec);

                // 길이 합산
                if (bq.getDurationMs() != null) totalMs += Math.max(0, bq.getDurationMs());
            }
        }

        // --- 버블 파일 합쳐서 rVoice 생성 ---
        if (!bubbleFiles.isEmpty()) {
            try {
                Path merged = mergeAudioFiles(bubbleFiles); // ffmpeg concat
                // NGINX alias /voices/ → /data/maumii/uploads/voices/ 라면:
                String publicUrlPath = "/voices/" + merged.getFileName();
                rec.setRVoice(publicUrlPath);
            } catch (Exception e) {
                log.error("voice merge failed", e);
                // 실패해도 최소 첫 파일이라도 지정하고 싶다면:
                Path first = bubbleFiles.get(0);
                rec.setRVoice("/voices/" + first.getFileName());
            }
        }

        // --- 총 길이 기록 ---
        if (totalMs > 0) {
            rec.setRLength(msToLocalTime(totalMs));
        }

        recordRepository.save(rec);
        return rec.getRId();
    }

    /* --------- 유틸 --------- */

    private RecordList resolveOrCreateRecordList(Long rlId, String newTitle, User owner) {
        if (rlId != null) {
            return recordListRepository.findById(rlId)
                    .orElseThrow(() -> new IllegalArgumentException("recordList not found: " + rlId));
        }
        if (newTitle != null && !newTitle.isBlank()) {
            RecordList rl = new RecordList();
            rl.setUser(owner);
            rl.setRlName(newTitle);
            return recordListRepository.save(rl);
        }
        // 아무 것도 없으면 “기본” 자동 생성
        RecordList rl = new RecordList();
        rl.setRlName("기본");
        rl.setUser(owner); // ⬅️ 리스트 소유자 저장
        return recordListRepository.save(rl);
    }

    private String saveVoice(MultipartFile file) {
        Path p = saveVoiceReturnPath(file);
        return "/voices/" + p.getFileName(); // 공개 URL 경로만 보관
    }

    private Path saveVoiceReturnPath(MultipartFile file) {
        String filename = UUID.randomUUID() + resolveExt(file);
        Path dst = root.resolve(filename);
        try {
            Files.copy(file.getInputStream(), dst, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("voice save failed", e);
        }
        return dst;
    }

    private String resolveExt(MultipartFile f) {
        String ct = (f.getContentType() == null ? "" : f.getContentType().toLowerCase(Locale.ROOT));
        if (ct.contains("ogg")) return ".ogg";
        if (ct.contains("webm")) return ".webm";
        if (ct.contains("wav")) return ".wav";
        if (ct.contains("mpeg") || ct.contains("mp3")) return ".mp3";
        return ".ogg"; // 기본은 .ogg 로
    }

    private java.time.LocalTime msToLocalTime(Long ms) {
        long sec = Math.max(0, ms / 1000);
        int nano = (int) ((ms % 1000) * 1_000_000);
        return java.time.LocalTime.ofSecondOfDay(sec).withNano(nano);
    }

    /** ffmpeg concat demuxer로 오디오 병합 (동일 코덱/샘플레이트 가정) */
    private Path mergeAudioFiles(List<Path> parts) throws IOException, InterruptedException {
        // concat 리스트 파일 생성
        Path listFile = Files.createTempFile("concat_", ".txt");
        StringBuilder sb = new StringBuilder();
        for (Path p : parts) {
            // 공백/특수문자 경로 안전하게: 싱글쿼트로 감싼다
            sb.append("file '").append(p.toAbsolutePath().toString().replace("'", "'\\''")).append("'\n");
        }
        Files.writeString(listFile, sb.toString());

        Path out = root.resolve(UUID.randomUUID() + ".ogg");

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-safe", "0",
                "-f", "concat",
                "-i", listFile.toAbsolutePath().toString(),
                "-c", "copy",            // 재인코딩 없이 붙이기
                out.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process pr = pb.start();

        // 로그 소비(버퍼 고갈 방지)
        try (var in = pr.getInputStream()) { in.transferTo(OutputStream.nullOutputStream()); }

        int exit = pr.waitFor();
        Files.deleteIfExists(listFile);
        if (exit != 0) throw new RuntimeException("ffmpeg concat failed, exit=" + exit);

        return out;
    }

    public List<RecordRes> getRecordsInList(Long rlId, String userIdOrNull) {
        List<Record> records = recordRepository.findAllWithBubblesByRecordListId(rlId);

        return records.stream()
                .map(RecordRes::toRecordRes)   // ✅ bubbles 포함해서 변환
                .toList();
    }

    //userId로 검증하는로직
//    public List<RecordRes> getRecordsInList(Long rlId, String uId) {
//        var rl = recordListRepository.findById(rlId)
//                .orElseThrow(() -> new IllegalArgumentException("recordList not found"));
//        if (!rl.getUser().getUId().equals(uId)) throw new IllegalArgumentException("not your list");
//
//        var records = recordRepository.findAllWithBubblesByRecordListIdAndUserUId(rlId, uId);
//        return records.stream().map(RecordRes::toRecordRes).toList();
//    }


}