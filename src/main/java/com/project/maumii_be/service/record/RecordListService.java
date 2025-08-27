package com.project.maumii_be.service.record;

import com.project.maumii_be.domain.RecordList;
import com.project.maumii_be.dto.RecordListReq;
import com.project.maumii_be.dto.RecordListRes;
import com.project.maumii_be.exception.DMLException;
import com.project.maumii_be.exception.RecordListSearchNotException;
import com.project.maumii_be.repository.RecordListRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordListService {
    private final RecordListRepository recordListRepository;

    // 녹음 리스트 조회
    public List<RecordListRes> findRecordLists(String uId) throws RecordListSearchNotException {
        List<RecordList> list = recordListRepository.findByUser_uIdOrderByRlIdDesc(uId);
        if(list.isEmpty())
            throw new RecordListSearchNotException("해당 사용자의 녹음 리스트가 없습니다.", "Not Found Record List");
        return list.stream()
                .map(rl -> new RecordListRes().toRecordListRes(rl)).collect(Collectors.toList());
    }

    // 녹음 리스트 삭제
    @Transactional
    public String deleteRecordList(Long rlId) throws RecordListSearchNotException {
        RecordList recordListEntity = recordListRepository.findById(rlId)
                .orElseThrow(()-> new DMLException("Record List 아이디 오류로 삭제 실패", "Wrong Record List Id"));
        // Bubble 삭제 추가하기
        // Record 삭제 추가하기
        recordListRepository.deleteById(rlId);
        return "Record List DELETE OK";
    }

    // 녹음 리스트 이름 변경 ... update
    @Transactional
    public RecordListRes updateRecordList(Long rlId, RecordListReq recordListReq) throws RecordListSearchNotException {
        RecordList recordListEntity = recordListRepository.findById(rlId)
                .orElseThrow(()-> new DMLException("Record List 아이디 오류로 수정 실패", "Wrong Record List Id"));
        recordListEntity.setRlName(recordListReq.getRlName());
        return new RecordListRes().toRecordListRes(recordListEntity);
    }
}