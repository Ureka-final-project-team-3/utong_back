package com.ureka.team3.utong_backend.auth.repository;



import java.util.Optional;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ureka.team3.utong_backend.auth.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);
    
    @Query("SELECT a FROM Account a WHERE a.provider = :provider AND a.providerId = :providerId")
    Account findByProviderAndProviderId(@Param("provider") String provider, @Param("providerId") String providerId);

    // 비관적 락을 사용하여 계정 정보를 조회
    // 타임아웃을 3초로 설정, 초과 시 예외 발생
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT a FROM Account a WHERE a.id = :accountId")
    Optional<Account> findByIdWithLock(@Param("accountId") String accountId);
}