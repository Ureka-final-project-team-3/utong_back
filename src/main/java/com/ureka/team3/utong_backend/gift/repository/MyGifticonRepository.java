package com.ureka.team3.utong_backend.gift.repository;

import com.ureka.team3.utong_backend.gift.entity.UserGifticon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


// 마이 기프티콘 목록
public interface MyGifticonRepository extends JpaRepository<UserGifticon, UUID> {
    // User 엔티티 안의 id를 기준으로 조회
    List<UserGifticon> findByUser_Id(String userId);

    // 기프티콘 상세
    Optional<UserGifticon> findByIdAndUser_Id(String id, String userId);

}


