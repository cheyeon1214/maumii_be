package com.project.maumii_be.repository;

import com.project.maumii_be.domain.Protector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProtectorRepository extends JpaRepository<Protector, Long> {
    List<Protector> findByUser_uId(String userId);
}
