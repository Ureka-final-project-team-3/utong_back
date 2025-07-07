package com.ureka.team3.utong_backend.auth.repository;



import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ureka.team3.utong_backend.auth.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);
    
    @Query("SELECT a FROM Account a WHERE a.provider = :provider AND a.providerId = :providerId")
    Account findByProviderAndProviderId(@Param("provider") String provider, @Param("providerId") String providerId);
}