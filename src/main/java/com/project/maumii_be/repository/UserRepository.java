package com.project.maumii_be.repository;

import com.project.maumii_be.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u from User u where u.uId = :uId and u.uPwd = :uPwd")
    Optional<User> login(@Param("uId") String uId,
                             @Param("uPwd") String uPwd);
}
