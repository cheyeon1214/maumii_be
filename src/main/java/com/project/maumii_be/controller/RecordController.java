package com.project.maumii_be.controller;

import com.project.maumii_be.dto.bubble.BubbleUpdateReq;
import com.project.maumii_be.dto.record.CreateRecordReq;
import com.project.maumii_be.dto.record.RecordRes;
import com.project.maumii_be.dto.recordlist.RecordListReq;
import com.project.maumii_be.dto.recordlist.RecordListRes;
import com.project.maumii_be.dto.user.UserRes;
import com.project.maumii_be.jwt.JWTUtil;
import com.project.maumii_be.service.record.BubbleService;
import com.project.maumii_be.service.record.RecordListService;
import com.project.maumii_be.service.record.RecordSaveService;
import com.project.maumii_be.service.record.RecordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5050", maxAge = 18000)
@RestController
@RequestMapping("/api/records")
@Tag(name = "Record API", description = "녹음 및 녹음 관리 관련 API")
@RequiredArgsConstructor
@Slf4j
public class RecordController {
    private final RecordListService recordListService;
    private final RecordService recordService;
    private final BubbleService bubbleService;
    private final RecordSaveService recordSaveService;
    private final com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();

    // 녹음 리스트 페이지에서 전체 녹음 리스트 조회하기
    @GetMapping("/{uId}/record-list")
    public ResponseEntity<?> findRecordLists(@PathVariable String uId) {
        return new ResponseEntity<>(recordListService.findRecordLists(uId), HttpStatus.OK);
    }

    // 녹음 리스트 페이지에서 이름으로 리스트 검색하기
    @GetMapping("/{uId}/record-list/{keyword}")
    public ResponseEntity<?> findRecordLists(@PathVariable String uId, @PathVariable String keyword) {
        return new ResponseEntity<>(recordListService.findRecordLists(uId, keyword), HttpStatus.OK);
    }

    // 녹음 저장 모달에서 녹음 리스트 이름만 조회하기
    @GetMapping("/{uId}/record-name")
    public ResponseEntity<?> findRecordNames(@PathVariable String uId) {
        List<RecordListRes> list = recordListService.findRecordLists(uId);
        List<Map<String, ?>> nameList = list.stream()
                .map(dto -> Map.of(
                        "rlId", dto.getRlId(),
                        "rlName", dto.getRlName()
                )).collect(Collectors.toList());
        log.info("이름만 조회하기 테스트용 => "+nameList.toString());
        return new ResponseEntity<>(nameList, HttpStatus.OK);
    }

    // 녹음 리스트 삭제하기
    @DeleteMapping("/record-list/{rlId}")
    public ResponseEntity<?> deleteRecordList(@PathVariable Long rlId) {
        return new ResponseEntity<>(recordListService.deleteRecordList(rlId), HttpStatus.OK);
    }

    // 녹음 리스트 이름 변경하기
    @PutMapping("/record-list/{rlId}")
    public ResponseEntity<?> updateRecordList(@PathVariable Long rlId, @RequestBody RecordListReq recordListReq) {
        return new ResponseEntity<>(recordListService.updateRecordList(rlId, recordListReq), HttpStatus.ACCEPTED);
    }

    // 녹음 저장하기 ... 녹음 리스트, 녹음, 버블 테이블에 모두 들어가야 함

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> saveRecord(
            @RequestPart("record") CreateRecordReq payload,
            @RequestParam(required = false) MultiValueMap<String, MultipartFile> files,
            HttpServletRequest request,
            JWTUtil jwtUtil
    ) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "NO_TOKEN"));
        }
        String token = auth.substring(7);
        String uId;
        try {
            uId = jwtUtil.getUId(token); // 또는 getUserId(token) 등 네 유틸 메서드
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "INVALID_TOKEN"));
        }

        Long id = recordSaveService.saveRecordWithBubbles(payload, files, uId);
        return ResponseEntity.ok(Map.of("recordId", id));
    }

    // 녹음 리스트 기준으로 녹음s 조회
    @GetMapping("/{rlId}")
    public ResponseEntity<List<RecordRes>> getRecordsByRecordList(
            @PathVariable Long rlId,
            @RequestParam(required = false) String userId // 보안 적용 시 SecurityContext에서 꺼내도 됨
    ) {
        List<RecordRes> list = recordSaveService.getRecordsInList(rlId, userId);
        return ResponseEntity.ok(list);
    }

    // 녹음 파일 조회하기 (음성 파일 포함)
    @GetMapping("/record-voice/{rId}")
    public ResponseEntity<?> findRecordVoice(@PathVariable Long rId) {
        return new ResponseEntity<>(recordService.findRecordVoice(rId), HttpStatus.OK);
    }

    // 녹음 리스트의 상세 대화 조회하기
    @GetMapping("/record-list/{rlId}")
    public ResponseEntity<?> getRecord(@PathVariable Long rlId) {
        return new ResponseEntity<>(bubbleService.getRecord(rlId), HttpStatus.OK);
    }

    // 녹음 다중 삭제하기
    @DeleteMapping("/record")
    public ResponseEntity<?> deleteRecords(@RequestBody List<Long> rIds) {
        return new ResponseEntity<>(recordService.deleteByRIdIn(rIds), HttpStatus.OK);
    }

    // bId 기준으로 버블 정보 조회하기
    @GetMapping("/bubble/{bId}")
    public ResponseEntity<?> findBubble(@PathVariable Long bId) {
        return new ResponseEntity<>(bubbleService.findById(bId), HttpStatus.OK);
    }

    // 버블 텍스트 및 감정 수정하기
    @PutMapping("/bubble/{bId}")
    public ResponseEntity<?> updateBubble(@PathVariable Long bId, @RequestBody BubbleUpdateReq bubbleUpdateReq) {
        return new ResponseEntity<>(bubbleService.updateBubble(bId, bubbleUpdateReq), HttpStatus.ACCEPTED);
    }
}