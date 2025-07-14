package com.ureka.team3.utong_backend.roulette.repository;

import com.ureka.team3.utong_backend.roulette.entity.RouletteEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RouletteEventRepository extends JpaRepository<RouletteEvent, String> {
    
    @Query("SELECT re FROM RouletteEvent re WHERE re.isActive = true AND CURRENT_TIMESTAMP BETWEEN re.startDate AND re.endDate ORDER BY re.createdAt DESC")
    Optional<RouletteEvent> findActiveEvent();
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT re FROM RouletteEvent re WHERE re.id = :eventId")
    Optional<RouletteEvent> findByIdWithLock(@Param("eventId") String eventId);
}