//package com.project.maumii_be.controller;
//
//import com.project.maumii_be.dto.CreateRecordReq;
//import com.project.maumii_be.service.record.RecordService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Map;
//
//@CrossOrigin(origins = "http://localhost:5050")
//@RestController
//@RequestMapping("/api/records")
//@RequiredArgsConstructor
//public class RecordController {
//    private final RecordService recordService;
//
//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<Map<String,Object>> createRecord(
//            @RequestPart("record") CreateRecordReq req,
//            @RequestParam MultiValueMap<String, MultipartFile> files
//    ) {
//        Long id = recordService.createRecord(req, files);
//        return ResponseEntity.ok(Map.of("id", id));
//    }
//}