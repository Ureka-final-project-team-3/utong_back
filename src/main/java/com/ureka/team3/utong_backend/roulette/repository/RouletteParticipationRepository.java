package com.ureka.team3.utong_backend.roulette.repository;

import com.ureka.team3.utong_backend.roulette.entity.RouletteParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RouletteParticipationRepository extends JpaRepository<RouletteParticipation, String> {
    
    @Query("SELECT rp FROM RouletteParticipation rp WHERE rp.event.id = :eventId AND rp.account.id = :accountId")
    Optional<RouletteParticipation> findByEventIdAndAccountId(
        @Param("eventId") String eventId, 
        @Param("accountId") String accountId
    );


    @Query("""
    SELECT COUNT(rp) > 0
    FROM RouletteParticipation rp
    WHERE rp.event.id = :eventId
      AND rp.account.id = :accountId
      AND rp.participatedAt >= CURRENT_DATE
""")

    boolean existsByEventIdAndAccountId(String eventId, String accountId);
}