package com.project.maumii_be.service.record;

import com.project.maumii_be.domain.Bubble;
import com.project.maumii_be.domain.Record;
import com.project.maumii_be.domain.RecordList;
import com.project.maumii_be.domain.enums.Emotion;
import com.project.maumii_be.dto.CreateRecordReq;
import com.project.maumii_be.repository.RecordListRepository;
import com.project.maumii_be.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.util.Locale;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Transactional
public class RecordService {

    private final RecordRepository recordRepository;
    private final RecordListRepository recordListRepository;
    private final Path root = Paths.get("uploads/voices");

    public Long createRecord(CreateRecordReq req,
                             MultiValueMap<String, MultipartFile> files) {

        try { Files.createDirectories(root); } catch (IOException ignore) {}

        Record rec = new Record();

        // (옵션) 어떤 RecordList에 넣을지
        if (req.recordListId() != null) {
            RecordList rl = recordListRepository.findById(req.recordListId())
                    .orElseThrow(() -> new IllegalArgumentException("recordList not found: " + req.recordListId()));
            rec.setRecordList(rl);
        }

        // (옵션) 세션 통짜 오디오 파일이 올라오면 r_voice로 보관
        if (req.rVoiceField() != null) {
            MultipartFile rv = files.getFirst(req.rVoiceField());
            if (rv != null && !rv.isEmpty()) {
                rec.setRVoice(saveVoice(rv));  // 경로 문자열
            }
        }

        long sumMs = 0L;

        if (req.bubbles() != null) {
            for (var dto : req.bubbles()) {
                Bubble b = new Bubble();
                b.setRecord(rec);                            // FK
                b.setBTalker(Boolean.TRUE.equals(dto.talker())); // true=내 말, false=상대
                if (dto.text() != null) b.setBText(dto.text());
                if (dto.lengthMs() != null) {
                    b.setBLength(msToLocalTime(dto.lengthMs()));
                    sumMs += Math.max(0L, dto.lengthMs());
                }
                // 감정 없으면 calm
                b.setBEmotion(parseEmotion(dto.emotion()));

                // ★ 연관관계 편의 메서드: cascade로 같이 저장됨
                rec.getBubbles().add(b);
            }
        }

        // 세션 총 길이 (요청에 없으면 서버에서 합산한 값으로 세팅)
        if (rec.getRLength() == null && sumMs > 0) {
            rec.setRLength(msToLocalTime(sumMs));
        }

        recordRepository.save(rec);
        return rec.getRId();
    }

    private String saveVoice(MultipartFile file) {
        String filename = UUID.randomUUID() + resolveExt(file);
        Path dst = root.resolve(filename);
        try (var in = file.getInputStream()) {
            Files.copy(in, dst, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("voice save failed", e);
        }
        return dst.toString();
    }

    private String resolveExt(MultipartFile f) {
        String ct = (f.getContentType() == null ? "" : f.getContentType().toLowerCase(Locale.ROOT));
        if (ct.contains("ogg")) return ".ogg";
        if (ct.contains("webm")) return ".webm";
        if (ct.contains("wav")) return ".wav";
        if (ct.contains("mpeg") || ct.contains("mp3")) return ".mp3";
        return ".bin";
    }

    private LocalTime msToLocalTime(Long ms) {
        long sec = Math.max(0, ms / 1000);
        int nano = (int) ((ms % 1000) * 1_000_000);
        return LocalTime.ofSecondOfDay(sec).withNano(nano);
    }

    private Emotion parseEmotion(String s) {
        if (s == null || s.isBlank()) return Emotion.calm;
        try { return Emotion.valueOf(s.trim().toLowerCase(Locale.ROOT)); }
        catch (Exception ignore) { return Emotion.calm; }
    }
}