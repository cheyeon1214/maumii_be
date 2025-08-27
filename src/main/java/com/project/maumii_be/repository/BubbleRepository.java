package com.project.maumii_be.repository;

import com.project.maumii_be.domain.Bubble;
import com.project.maumii_be.domain.Bubble;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BubbleRepository extends JpaRepository<Bubble, Long> {

    // 녹음 리스트 상세 조회
    @Query(value = "select b from Bubble b join fetch b.record r join fetch r.recordList rl where rl.rlId = :rlId order by r.rId desc, b.bId asc")
    List<Bubble> getRecord(Long rlId);

    // 버블 정보 조회
    Optional<Bubble> findById(Long bId);

    // 버블 텍스트 및 감정 수정 ... update

    // 특정 레코드 번호에 해당하는 버블 전체 삭제
    @Query(value = "delete from Bubble b where b.record.rId = :rId")
    void deleteByRId(Long rId);
}
