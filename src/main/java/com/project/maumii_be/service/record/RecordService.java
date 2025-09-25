// src/main/java/com/project/maumii_be/service/record/RecordService.java
package com.project.maumii_be.service.record;

import com.project.maumii_be.domain.Record;
import com.project.maumii_be.dto.RecordRes;
import com.project.maumii_be.exception.DMLException;
import com.project.maumii_be.exception.RecordSearchNotException;
import com.project.maumii_be.repository.RecordListRepository;
import com.project.maumii_be.repository.RecordRepository;
import com.project.maumii_be.repository.BubbleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.*;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecordService {

    private final BubbleRepository bubbleRepository;
    private final RecordRepository recordRepository;
    private final RecordListRepository recordListRepository;

    // 업로드 루트(컨테이너/서버 내부 실제 경로)
    private final Path uploadRoot = Paths.get(System.getProperty("app.upload.dir", "uploads/voices"));
    // DB에 저장하는 공개 경로 접두어
    private static final String PUBLIC_PREFIX = "/voices/";

    /** 공개 경로(/voices/xxx.wav) -> 서버 실제 파일 경로 */
    private Path toRealPath(String publicPath) {
        if (publicPath == null || publicPath.isBlank()) return null;
        String name = publicPath.startsWith(PUBLIC_PREFIX)
                ? publicPath.substring(PUBLIC_PREFIX.length())
                : publicPath; // 혹시 절대경로가 저장됐다면 그대로 이름만 추려서
        return uploadRoot.resolve(name);
    }

    // ---------- 삭제: DB + 서버 파일 ----------
    public String deleteByRIdIn(Collection<Long> rIds) throws RecordSearchNotException {
        List<Record> list = recordRepository.findByrIdIn(rIds);

        // 1) 파일 삭제 (없어도 무시)
        for (Record r : list) {
            try {
                Path file = toRealPath(r.getRVoice());
                if (file != null) {
                    Files.deleteIfExists(file);
                }
            } catch (Exception e) {
                // 파일이 이미 없어도 서비스는 계속 진행
                log.warn("[record-delete] file delete fail for rId={}, path={}, cause={}",
                        r.getRId(), r.getRVoice(), e.toString());
            }
        }

        // 2) 레코드 삭제 (버블은 JPA cascade로 함께 삭제)
        recordRepository.deleteAll(list);

        return "Record DELETE OK";
    }

    // ---------- 재생용 조회: DB에서 공개 경로만 돌려주면 프론트가 그대로 재생 ----------
    @Transactional(readOnly = true)
    public RecordRes findRecordVoice(Long rId) {
        Record record = recordRepository.findById(rId)
                .orElseThrow(() -> new DMLException("Record 아이디 오류로 조회 실패", "Wrong Record Id"));
        return new RecordRes().toRecordRes(record); // 여기 안에 rVoice(= /voices/xxx.wav) 포함
    }
}