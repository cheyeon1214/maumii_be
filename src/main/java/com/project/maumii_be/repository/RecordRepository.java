package com.project.maumii_be.repository;

import com.project.maumii_be.domain.Record;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecordRepository extends JpaRepository<Record, Long> {

    // 녹음 저장
    Record save(Record record);

    // 녹음 삭제
    void deleteById(Long rId);

    // 녹음(음성) 파일 조회
    Optional<Record> findById(Long rId);
}
