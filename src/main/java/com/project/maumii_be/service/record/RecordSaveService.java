package com.project.maumii_be.service.record;

import com.project.maumii_be.domain.Bubble;
import com.project.maumii_be.domain.Record;
import com.project.maumii_be.domain.RecordList;
import com.project.maumii_be.domain.enums.Emotion;
import com.project.maumii_be.dto.BubbleReq;
import com.project.maumii_be.dto.CreateRecordReq;
import com.project.maumii_be.dto.RecordReq;
import com.project.maumii_be.repository.BubbleRepository;
import com.project.maumii_be.repository.RecordListRepository;
import com.project.maumii_be.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecordSaveService {
    private final BubbleRepository bubbleRepository;
    private final RecordRepository recordRepository;
    private final RecordListRepository recordListRepository;
    private final Path root = Paths.get("uploads/voices");


    public Long saveRecordWithBubbles(CreateRecordReq payload,
                                      MultiValueMap<String, MultipartFile> files) {

        // 준비: 디렉터리
        try { Files.createDirectories(root); } catch (IOException ignore) {}

        // 1) Record 생성
        RecordReq rreq = payload.record();
        if (rreq == null) throw new IllegalArgumentException("record is required");

        // rlId 유효성 (선택)
        RecordList rl = null;
        if (rreq.getRlId() != null) {
            rl = recordListRepository.findById(rreq.getRlId())
                    .orElseThrow(() -> new IllegalArgumentException("recordList not found: " + rreq.getRlId()));
        }

        com.project.maumii_be.domain.Record rec = rreq.toRecord(rreq); // 네가 만든 toRecord 사용
        if (rl != null) rec.setRecordList(rl);

        // (옵션) 음성 파일 저장: payload.voiceField 로 찾음
        if (payload.voiceField() != null) {
            MultipartFile f = files.getFirst(payload.voiceField());
            if (f != null && !f.isEmpty()) {
                String path = saveVoice(f);
                rec.setRVoice(path);
            }
        }

        // 2) Bubble 생성 & 연관관계 설정
        if (payload.bubbles() != null && !payload.bubbles().isEmpty()) {
            for (BubbleReq bq : payload.bubbles()) {
                Bubble b = bq.toBubble(bq);
                b.setRecord(rec);               // FK 지정
                rec.getBubbles().add(b);        // 양방향(편의) - cascade면 자동 저장됨
            }
        }

        // 3) 저장
        recordRepository.save(rec);

        // cascade가 없다면 아래처럼 명시 저장
        // bubbleRepository.saveAll(rec.getBubbles());

        return rec.getRId();
    }

    public int addBubbles(Long recordId, List<BubbleReq> bubbles) {
        Record rec = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("record not found: " + recordId));
        int cnt = 0;
        if (bubbles != null) {
            for (BubbleReq bq : bubbles) {
                Bubble b = bq.toBubble(bq);
                b.setRecord(rec);
                bubbleRepository.save(b);
                cnt++;
            }
        }
        return cnt;
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
