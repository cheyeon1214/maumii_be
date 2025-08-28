package com.project.maumii_be.repository;

import com.project.maumii_be.domain.RecordList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecordListRepository extends JpaRepository<RecordList, Long> {

    // 녹음 리스트 조회
    @Query(value = "select rl from RecordList rl join fetch rl.user u where u.uId = :userId order by rl.updateDate desc")
    List<RecordList> findByUser_uIdOrderByRlIdDesc(String userId);

    // 날짜에 해당하는 레코드 리스트 조회

    // 단어로 검색하여 레코드 리스트 조회
    @Query(value="select rl from RecordList rl join fetch rl.user u where u.uId = :userId and rl.rlName like :keyword order by rl.updateDate desc")
    List<RecordList> findByRlNameLike(String userId, String keyword);

    // 녹음 리스트 삭제
    // void deleteById(Long rlId);

    // 녹음 리스트 이름 변경 ... update
}
