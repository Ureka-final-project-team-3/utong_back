package com.ureka.team3.utong_backend.line.repository;

import com.ureka.team3.utong_backend.line.entity.Line;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LineRepository extends JpaRepository<Line, String> {
    Optional<Line> findByUserId(String userId);
}
