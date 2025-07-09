package com.ureka.team3.utong_backend.line.repository;

import com.ureka.team3.utong_backend.line.entity.LineData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LineDataRepository extends JpaRepository<LineData, String> {
    Optional<LineData> findTopByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);
}
