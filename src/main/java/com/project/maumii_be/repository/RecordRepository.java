package com.project.maumii_be.repository;

import com.project.maumii_be.domain.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecordRepository extends JpaRepository<Record, Long> {

    // 녹음 저장
    // Record save(Record record);

    // 녹음 삭제
    // void deleteById(Long rId);

    // 녹음(음성) 파일 조회
    // Optional<Record> findById(Long rId);

    // 특정 레코드 리스트 번호에 해당하는 레코드 전체 삭제
    @Modifying
    @Query(value = "delete from Record r where r.recordList.rlId = :rlId")
    void deleteByRlId(Long rlId);

    // rlId로 해당 리스트의 Record와 그 하위 Bubbles까지 한 방에 로드
    @Query("""
      select distinct r
      from Record r
      left join fetch r.bubbles b
      where r.recordList.rlId = :rlId
      order by r.rId desc, b.bId asc
    """)
    List<Record> findAllWithBubblesByRecordListId(@Param("rlId") Long rlId);

    // rlId 의 레코드 리스트에 저장된 가장 최근 녹음의 가장 첫 버블 조회
}
