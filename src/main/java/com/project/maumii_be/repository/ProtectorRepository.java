package com.project.maumii_be.repository;

import com.project.maumii_be.domain.Protector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProtectorRepository extends JpaRepository<Protector, Long> {
    @Query(value="select p from Protector p where p.user.uId = :uId")
    List<Protector> findByUser_uId(@Param("uId") String uId);

    @Query(value="select (count(p) > 0) from Protector p where p.user.uId = :uId and p.pEmail = :pEmail")
    boolean existsByUserIdAndEmail(@Param("uId") String uId, @Param("pEmail") String pEmail);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value="delete from Protector p where p.pId = :pId and p.user.uId = :uId")
    int deleteByPIdAndUser_uId(Long pId, String userUId);
}
