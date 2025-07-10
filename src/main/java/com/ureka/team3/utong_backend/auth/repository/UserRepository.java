package com.ureka.team3.utong_backend.auth.repository;


import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ureka.team3.utong_backend.auth.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    @Query("SELECT u FROM User u WHERE u.account.id = :accountId")
    Optional<User> findByAccountId(@Param("accountId") String accountId);
    
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE user SET account_id = :accountId WHERE id = :userId", nativeQuery = true)
    void updateAccountId(@Param("userId") String userId, @Param("accountId") String accountId);
    Optional<User> findByNameAndBirthDate(String name, LocalDate birthDate);
}