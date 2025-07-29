package com.ureka.team3.utong_backend.gift.repository;

import com.ureka.team3.utong_backend.gift.entity.Gifticon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GifticonRepository extends JpaRepository<Gifticon, String> {

}
