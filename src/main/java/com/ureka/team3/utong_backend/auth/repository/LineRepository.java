package com.ureka.team3.utong_backend.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ureka.team3.utong_backend.auth.entity.Line;

@Repository
public interface LineRepository extends JpaRepository<Line, String> {
    
    @Query("SELECT l FROM Line l WHERE l.phoneNumber = :phoneNumber")
    Optional<Line> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    @Query("SELECT l.user.account.email FROM Line l WHERE l.phoneNumber = :phoneNumber")
    Optional<String> findEmailByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}