// src/main/java/com/project/maumii_be/service/record/RecordSaveService.java
package com.project.maumii_be.service.record;

import com.project.maumii_be.domain.Bubble;
import com.project.maumii_be.domain.Record;
import com.project.maumii_be.domain.RecordList;
import com.project.maumii_be.dto.BubbleReq;
import com.project.maumii_be.dto.CreateRecordReq;
import com.project.maumii_be.dto.RecordReq;
import com.project.maumii_be.repository.RecordListRepository;
import com.project.maumii_be.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecordSaveService {

    private final RecordRepository recordRepository;
    private final RecordListRepository recordListRepository;

    private final Path root = Paths.get("uploads/voices");

    // RecordSaveService.java (핵심 부분만)
    public Long saveRecordWithBubbles(CreateRecordReq payload,
                                      MultiValueMap<String, MultipartFile> files) {
        try { Files.createDirectories(root); } catch (IOException ignore) {}

        RecordReq rreq = payload.record();
        if (rreq == null) throw new IllegalArgumentException("record is required");

        // ✅ rlId 있으면 조회, 없으면 payload.recordListTitle로 생성
        RecordList recordList = resolveOrCreateRecordList(rreq.getRlId(), payload.recordListTitle());

        Record rec = rreq.toRecord(rreq);
        rec.setRecordList(recordList);
        if (rec.getBubbles() == null) rec.setBubbles(new ArrayList<>());

        if (payload.voiceField() != null) {
            MultipartFile vf = files.getFirst(payload.voiceField());
            if (vf != null && !vf.isEmpty()) rec.setRVoice(saveVoice(vf));
        }

        if (payload.bubbles() != null && !payload.bubbles().isEmpty()) {
            for (BubbleReq bq : payload.bubbles()) {
                Bubble b = bq.toBubble(bq);

                // 필수 보정
                if (b.getBTalker() == null || b.getBTalker().isBlank()) b.setBTalker("partner");
                if (b.getBLength() == null && bq.getDurationMs() != null) {
                    b.setBLength(msToLocalTime(bq.getDurationMs()));
                }

                b.setRecord(rec);
                rec.getBubbles().add(b);
            }
        }

        recordRepository.save(rec);
        return rec.getRId();
    }

    private RecordList resolveOrCreateRecordList(Long rlId, String title) {
        if (rlId != null) {
            return recordListRepository.findById(rlId)
                    .orElseThrow(() -> new IllegalArgumentException("recordList not found: " + rlId));
        }
        // ✅ 제목이 들어왔으면 그대로 사용, 없으면 기본 이름
        String name = (title != null && !title.isBlank())
                ? title
                : "세션 " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        RecordList newList = new RecordList();
        newList.setRlName(name);            // 엔티티 필드명에 맞게
        return recordListRepository.save(newList); // 먼저 save로 영속화
    }

    /** rlId가 있으면 조회해서 반환. 없고 title이 있으면 생성 후 save 해서 반환. */
    private RecordList resolveRecordList(Long rlId, String title) {
        if (rlId != null) {
            return recordListRepository.findById(rlId)
                    .orElseThrow(() -> new IllegalArgumentException("recordList not found: " + rlId));
        }
        if (title != null && !title.isBlank()) {
            RecordList newList = new RecordList();
            // 엔티티 필드명에 맞게 세팅하세요. 예: setRlName / setTitle
            newList.setRlName(title);  // ← 필드명이 title이면 setTitle(title)로 바꾸세요.
            return recordListRepository.save(newList); // ⭐ 먼저 저장
        }
        // 리스트 없이도 저장을 허용하려면 null 반환
        return null;
    }

    /* ----------------------- 내부 유틸 ----------------------- */

    private String saveVoice(MultipartFile file) {
        String filename = UUID.randomUUID() + resolveExt(file);
        Path dst = root.resolve(filename);
        try {
            Files.copy(file.getInputStream(), dst, StandardCopyOption.REPLACE_EXISTING);
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
}