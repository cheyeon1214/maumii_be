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

    // 업로드 실제 저장 루트(컨테이너/서버 경로)
    private final Path root = Paths.get(System.getProperty("app.upload.dir", "uploads/voices"));
    // 클라이언트에 돌려줄 공개 URL prefix
    private static final String PUBLIC_PREFIX = "/voices/";

    // ---------------------- 핵심 변경: 모두 WAV로 저장 ----------------------

    public Long saveRecordWithBubbles(CreateRecordReq payload,
                                      MultiValueMap<String, MultipartFile> files,
                                      String uId) {
        try { Files.createDirectories(root); } catch (IOException ignore) {}

        RecordReq rreq = payload.record();
        if (rreq == null) throw new IllegalArgumentException("record is required");

        var owner = userRepository.findById(uId)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + uId));

        RecordList recordList = resolveOrCreateRecordList(rreq.getRlId(), payload.recordListTitle(), owner);

        Record rec = rreq.toRecord(rreq);
        rec.setRecordList(recordList);
        if (rec.getBubbles() == null) rec.setBubbles(new ArrayList<>());

        // (선택) 세션 통짜 오디오가 따로 올 때 → WAV로 변환 저장
        if (payload.voiceField() != null) {
            MultipartFile vf = files.getFirst(payload.voiceField());
            if (vf != null && !vf.isEmpty()) {
                Path wav = transcodeToWav(vf);     // ★ 변환
                rec.setRVoice(PUBLIC_PREFIX + wav.getFileName());
            }
        }

        // 버블 처리
        List<Path> bubbleWavs = new ArrayList<>();
        long totalMs = 0L;

        if (payload.bubbles() != null && !payload.bubbles().isEmpty()) {
            for (BubbleReq bq : payload.bubbles()) {
                // 파일 → WAV 변환 저장
                MultipartFile f = (bq.getFileField() == null ? null : files.getFirst(bq.getFileField()));
                if (f != null && !f.isEmpty()) {
                    Path wav = transcodeToWav(f);   // ★ 변환
                    bubbleWavs.add(wav);
                }

                // 엔티티 변환/보정
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

        // 버블 파일들 WAV로 합쳐 rVoice 생성
        if (!bubbleWavs.isEmpty() && rec.getRVoice() == null) {
            try {
                Path merged = mergeWavFiles(bubbleWavs);  // ★ WAV concat
                rec.setRVoice(PUBLIC_PREFIX + merged.getFileName());
            } catch (Exception e) {
                log.error("voice merge failed", e);
                // 실패시 첫 파일 경로라도 지정
                rec.setRVoice(PUBLIC_PREFIX + bubbleWavs.get(0).getFileName());
            }
        }

        if (totalMs > 0) rec.setRLength(msToLocalTime(totalMs));

        recordRepository.save(rec);
        return rec.getRId();
    }

    // ---- 단일 파일을 서버에 WAV(16k, mono, PCM)로 변환 저장 ----
    private Path transcodeToWav(MultipartFile file) {
        try {
            // 원본을 임시 저장
            Path temp = Files.createTempFile("raw_", ".bin");
            try (var in = file.getInputStream()) {
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            }

            // 최종 목적지: uploads/voices/{uuid}.wav
            Path dst = root.resolve(UUID.randomUUID() + ".wav");

            // ffmpeg 변환 (16kHz, mono, PCM 16-bit little endian)
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-i", temp.toAbsolutePath().toString(),
                    "-vn",
                    "-acodec", "pcm_s16le",
                    "-ar", "16000",
                    "-ac", "1",
                    dst.toAbsolutePath().toString()
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            try (var in = p.getInputStream()) {
                in.transferTo(OutputStream.nullOutputStream());
            }
            int exit = p.waitFor();
            Files.deleteIfExists(temp);
            if (exit != 0) throw new RuntimeException("ffmpeg transcode failed, exit=" + exit);

            return dst;
        } catch (Exception e) {
            throw new RuntimeException("voice transcode failed", e);
        }
    }

    /** WAV 파일들을 하나로 합치기 (모두 16k/mono/pcm_s16le 가정) */
    private Path mergeWavFiles(List<Path> wavs) throws IOException, InterruptedException {
        // concat list.txt 생성
        Path listFile = Files.createTempFile("concat_", ".txt");
        StringBuilder sb = new StringBuilder();
        for (Path p : wavs) {
            sb.append("file '").append(p.toAbsolutePath().toString().replace("'", "'\\''")).append("'\n");
        }
        Files.writeString(listFile, sb.toString());

        Path out = root.resolve(UUID.randomUUID() + ".wav");

        // 같은 포맷이므로 concat demuxer + copy 가능, 혹은 안전하게 재인코딩
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-safe", "0",
                "-f", "concat",
                "-i", listFile.toAbsolutePath().toString(),
                // 동일 포맷이면 -c copy로 빠르게 가능하지만, 안전하게 다시 지정:
                "-acodec", "pcm_s16le",
                "-ar", "16000",
                "-ac", "1",
                out.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process pr = pb.start();
        try (var in = pr.getInputStream()) {
            in.transferTo(OutputStream.nullOutputStream());
        }
        int exit = pr.waitFor();
        Files.deleteIfExists(listFile);
        if (exit != 0) throw new RuntimeException("ffmpeg concat failed, exit=" + exit);

        return out;
    }

    // 나머지 메서드(resolveOrCreateRecordList, msToLocalTime 등)는 그대로 유지


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
        return ".wav"; // 기본은 .ogg 로
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