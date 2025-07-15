package com.ureka.team3.utong_backend.line.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ureka.team3.utong_backend.line.entity.Line;
import com.ureka.team3.utong_backend.line.entity.LineData;

public interface LineDataRepository extends JpaRepository<LineData, String> {
    Optional<LineData> findTopByLineOrderByMonthDesc(Line line);
}
