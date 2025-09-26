package com.project.maumii_be.service.record;

import com.project.maumii_be.domain.Bubble;
import com.project.maumii_be.domain.Record;
import com.project.maumii_be.domain.RecordList;
import com.project.maumii_be.dto.RecordListReq;
import com.project.maumii_be.dto.RecordListRes;
import com.project.maumii_be.exception.DMLException;
import com.project.maumii_be.exception.RecordListSearchNotException;
import com.project.maumii_be.repository.RecordListRepository;
import com.project.maumii_be.repository.RecordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordListService {
    private final RecordListRepository recordListRepository;
    private final RecordRepository recordRepository;

    // 녹음 리스트 조회
    public List<RecordListRes> findRecordLists(String uId) throws RecordListSearchNotException {
        List<RecordList> list = recordListRepository.findByUser_uIdOrderByRlIdDesc(uId);
        // '녹음 리스트 없음'은 예외 던지지 않는 게 맞음
        return list.stream()
                .map(rl -> {
                    RecordListRes res = new RecordListRes().toRecordListRes(rl);
                    Bubble bubble = recordRepository.findRecentBubbleByRecordListId(res.getRlId())
                                    .stream().findFirst().orElse(null);
                    if(bubble != null)
                        res.setRlText(bubble.getBText());
                    else
                        res.setRlText("");
                    return res;
                }).collect(Collectors.toList());
    }

    // 단어로 검색하여 레코드 리스트 조회
    public List<RecordListRes> findRecordLists(String uId, String keyword) throws RecordListSearchNotException {
        List<RecordList> list= recordListRepository.findByRlNameLike(uId, "%"+keyword+"%");
        log.info("{} 단어로 검색 ===> ", keyword);
        // '검색 결과 없음'은 예외 던지지 않는 게 맞음
        return list.stream()
                .map(rl -> {
                    RecordListRes res = new RecordListRes().toRecordListRes(rl);
                    Bubble bubble = recordRepository.findRecentBubbleByRecordListId(res.getRlId())
                            .stream().findFirst().orElse(null);
                    if(bubble != null)
                        res.setRlText(bubble.getBText());
                    else
                        res.setRlText("");
                    return res;
                }).collect(Collectors.toList());
    }

    // 녹음 리스트 삭제
    @Transactional
    public String deleteRecordList(Long rlId) throws RecordListSearchNotException {
        RecordList recordListEntity = recordListRepository.findById(rlId)
                .orElseThrow(()-> new DMLException("Record List 아이디 오류로 삭제 실패", "Wrong Record List Id"));
        List<Record> records = recordRepository.findByRecordList_RlId(rlId);
        // Record 삭제 추가하기 -> Record 는 Bubble 과 양방향이라 cascade 삭제
        // JPQL 쿼리문으로 삭제하면 Bubble cascade 삭제됨
        recordRepository.deleteAll(records);
        // Record List 삭제하기
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