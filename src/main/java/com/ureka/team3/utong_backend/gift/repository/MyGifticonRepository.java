package com.ureka.team3.utong_backend.gift.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ureka.team3.utong_backend.gift.entity.UserGifticon;


// 마이 기프티콘 목록
public interface MyGifticonRepository extends JpaRepository<UserGifticon, String> {
    // User 엔티티 안의 id를 기준으로 조회
    List<UserGifticon> findByUser_Id(String userId);

    // 기프티콘 상세
    Optional<UserGifticon> findByIdAndUser_Id(String id, String userId);

    // 활성화된 기프티콘 & 유효 기간이 지난 기프티콘 조회
    List<UserGifticon> findByIsActiveTrueAndExpiredAtBefore(LocalDateTime now);
}


