package com.project.maumii_be.repository;

import com.project.maumii_be.domain.Protector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface ProtectorRepository extends JpaRepository<Protector, Long> {
    List<Protector> findByUser_uId(String userId);

    boolean existsByUser_uIdAndPEmail(String uId, String pEmail);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    int deleteByPIdAndUser_uId(Long pId, String userUId);
}
