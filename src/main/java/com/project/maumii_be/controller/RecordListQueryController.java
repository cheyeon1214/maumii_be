package com.project.maumii_be.controller;

import com.project.maumii_be.dto.RecordRes;
import com.project.maumii_be.service.record.RecordSaveService;
import com.project.maumii_be.service.record.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/record-lists")
@CrossOrigin(origins = "http://localhost:5050")
public class RecordListQueryController {

    private final RecordSaveService recordSaveService;

    // /api/record-lists/{rlId}/records
    @GetMapping("/{rlId}/records")
    public ResponseEntity<List<RecordRes>> getRecordsByRecordList(
            @PathVariable Long rlId,
            @RequestParam(required = false) String userId // 보안 적용 시 SecurityContext에서 꺼내도 됨
    ) {
        List<RecordRes> list = recordSaveService.getRecordsInList(rlId, userId);
        return ResponseEntity.ok(list);
    }
}