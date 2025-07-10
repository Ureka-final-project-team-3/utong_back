package com.ureka.team3.utong_backend.auth.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ureka.team3.utong_backend.auth.entity.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    @Query("SELECT p FROM PasswordResetToken p WHERE p.account.id = :accountId AND p.used = false AND p.expiredAt > :now")
    List<PasswordResetToken> findValidTokensByAccountId(@Param("accountId") String accountId, @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.account.id = :accountId")
    void markAllTokensAsUsedByAccountId(@Param("accountId") String accountId);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiredAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}