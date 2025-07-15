package com.ureka.team3.utong_backend.coupon.entity;

import com.ureka.team3.utong_backend.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupon")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCoupon {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "status", length = 10, nullable = false)
    private String status; // 공통코드 040: "001", "002", "003"
}
