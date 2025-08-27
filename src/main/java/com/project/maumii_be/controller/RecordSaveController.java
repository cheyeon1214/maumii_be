package com.project.maumii_be.controller;

import com.project.maumii_be.dto.CreateRecordReq;
import com.project.maumii_be.service.record.RecordSaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:5050")
@RestController
@RequestMapping("/api/records/save")
@RequiredArgsConstructor
@Slf4j
public class RecordSaveController {

    private final RecordSaveService recordSaveService;
    private final com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> saveRecord(
            @RequestPart("record") CreateRecordReq payload,
            @RequestParam(required = false) MultiValueMap<String, MultipartFile> files
    ) {
        // ✅ 전체 payload를 JSON으로 안전하게 로깅
        try {
            log.info("[save] payload json = {}", om.writeValueAsString(payload));
        } catch (Exception e) {
            log.warn("[save] payload stringify failed: {}", e.toString());
        }

        // ✅ 널 가드 및 핵심 필드 로그
        if (payload == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "record part is missing"));
        }
        if (payload.record() == null) {
            log.warn("[save] payload.record() is null");
        } else {
            log.info("[save] record.rlId={}, rLength={}, rVoice={}",
                    payload.record().getRlId(),
                    payload.record().getRLength(),
                    payload.record().getRVoice());
        }

        if (payload.bubbles() == null || payload.bubbles().isEmpty()) {
            log.warn("[save] bubbles is empty");
        } else {
            for (int i = 0; i < payload.bubbles().size(); i++) {
                var b = payload.bubbles().get(i);
                log.info("[save] bubble[{}] talker={}, text='{}', emotion={}, durationMs={}",
                        i, b.getBTalker(), b.getBText(), b.getBEmotion(), b.getDurationMs());
            }
        }

        // ✅ 파일 존재 여부만 간단 체크
        if (files != null) {
            log.info("[save] files keys = {}", files.keySet());
            files.forEach((k, v) -> log.info("  - {} : {} files", k, (v==null?0:v.size())));
        }

        Long id = recordSaveService.saveRecordWithBubbles(payload, files);
        return ResponseEntity.ok(Map.of("recordId", id));
    }
}