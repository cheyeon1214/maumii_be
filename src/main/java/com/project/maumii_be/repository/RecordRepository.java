package com.project.maumii_be.repository;

import com.project.maumii_be.domain.Record;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<Record, Long> {
}
