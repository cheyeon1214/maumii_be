package com.project.maumii_be.service.record;

import com.project.maumii_be.domain.Bubble;
import com.project.maumii_be.domain.Record;
import com.project.maumii_be.domain.RecordList;
import com.project.maumii_be.domain.enums.Emotion;
import com.project.maumii_be.dto.BubbleReq;
import com.project.maumii_be.dto.CreateRecordReq;
import com.project.maumii_be.dto.RecordReq;
import com.project.maumii_be.dto.RecordRes;
import com.project.maumii_be.exception.DMLException;
import com.project.maumii_be.exception.RecordSearchNotException;
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
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecordService {
    private final BubbleRepository bubbleRepository;
    private final RecordRepository recordRepository;
    private final RecordListRepository recordListRepository;
    private final Path root = Paths.get("uploads/voices");

    // 녹음 삭제
    @Transactional
    public String deleteByRIdIn(Collection<Long> rIds) throws RecordSearchNotException {
        List<Record> list = recordRepository.findByrIdIn(rIds);
        // 버블 삭제는 cascade
        // 녹음 삭제
        recordRepository.deleteAllInBatch(list); // deleteAll 도 가능하지만 대량 삭제에서 성능이 더 좋음
        return "Record DELETE OK";
    }

    // 녹음(음성) 파일 조회
    @Transactional(readOnly = true)
    public RecordRes findRecordVoice(Long rId) {
        Record record = recordRepository.findById(rId)
                .orElseThrow(() -> new DMLException("Record 아이디 오류로 삭제 실패", "Wrong Record Id"));
        return new RecordRes().toRecordRes(record);
    }

}