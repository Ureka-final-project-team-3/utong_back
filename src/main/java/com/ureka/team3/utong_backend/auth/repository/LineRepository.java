package com.ureka.team3.utong_backend.auth.repository;

import com.ureka.team3.utong_backend.line.entity.Line;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LineRepository extends JpaRepository<Line, String> {
    
    @Query("SELECT l FROM Line l WHERE l.phoneNumber = :phoneNumber")
    Optional<Line> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    @Query("SELECT l.user.account.email FROM Line l WHERE l.phoneNumber = :phoneNumber")
    Optional<String> findEmailByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    Optional<Line> findByUserId(String userId);

    // 특정 userId 가 가진 모든 회선(Line) 리스트로 조회
    List<Line> findAllByUserId(String userId);
    // 특정 유저가 해당 lineId 의 회선을 실제로 소유하는지 확인 있으면 true, 없으면 false
    boolean existsByIdAndUserId(String id, String userId);
}