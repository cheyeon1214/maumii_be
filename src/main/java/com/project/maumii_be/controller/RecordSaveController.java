package com.project.maumii_be.controller;

import com.project.maumii_be.domain.User;
import com.project.maumii_be.dto.CreateRecordReq;
import com.project.maumii_be.dto.UserRes;
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
            @RequestParam(required = false) MultiValueMap<String, MultipartFile> files,
            jakarta.servlet.http.HttpSession session
    ) {
        var su = (UserRes) session.getAttribute("LOGIN_USER");
        if (su == null) {
            return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        }
        String uId = su.getUId(); // 또는 getUId()

        Long id = recordSaveService.saveRecordWithBubbles(payload, files, uId); // ⬅️ 전달
        return ResponseEntity.ok(Map.of("recordId", id));
    }
}