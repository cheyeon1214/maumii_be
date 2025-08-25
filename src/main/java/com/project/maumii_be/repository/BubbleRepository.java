package com.project.maumii_be.repository;

import com.project.maumii_be.domain.Bubble;
import com.project.maumii_be.domain.Bubble;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BubbleRepository extends JpaRepository<Bubble, Long> {
    @Query(value = "select b from Bubble b join fetch b.record r join fetch r.recordList rl where rl.rlId = :rlId order by r.rId desc")
    List<Bubble> getRecord(Long rlId);
}
