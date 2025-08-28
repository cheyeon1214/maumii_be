package com.project.maumii_be.controller;

import com.project.maumii_be.dto.BubbleReq;
import com.project.maumii_be.dto.CreateRecordReq;
import com.project.maumii_be.dto.RecordListReq;
import com.project.maumii_be.dto.RecordListRes;
import com.project.maumii_be.service.record.BubbleService;
import com.project.maumii_be.service.record.RecordListService;
import com.project.maumii_be.service.record.RecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5050", maxAge = 18000)
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Slf4j
public class RecordController {
    private final RecordListService recordListService;
    private final RecordService recordService;
    private final BubbleService bubbleService;

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

    // RecordSaveController 에서 작업 후 합칠 예정..?
    // 녹음 저장하기
    // 녹음 리스트, 녹음, 버블 테이블에 모두 들어가야 함
    // 녹음 리스트 updateDate 갱신
    // 녹음 삭제하기
    // => 다중 선택 삭제가 가능해야 함 !!!

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

    // bId 기준으로 버블 정보 조히하기
    @GetMapping("/bubble/{bId}")
    public ResponseEntity<?> findBubble(@PathVariable Long bId) {
        return new ResponseEntity<>(bubbleService.findById(bId), HttpStatus.OK);
    }

    // 버블 텍스트 및 감정 수정하기
    @PutMapping("/bubble/{bId}")
    public ResponseEntity<?> updateBubble(@PathVariable Long bId, @RequestBody BubbleReq bubbleReq) {
        return new ResponseEntity<>(bubbleService.updateBubble(bId, bubbleReq), HttpStatus.ACCEPTED);
    }

}