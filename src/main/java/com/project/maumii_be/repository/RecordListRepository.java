package com.project.maumii_be.repository;

import com.project.maumii_be.domain.RecordList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordListRepository extends JpaRepository<RecordList, Long> {
    List<RecordList> findByUser_uIdOrderByRlIdDesc(String userId);
}
