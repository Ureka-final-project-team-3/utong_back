package com.ureka.team3.utong_backend.mypage.entity;

import com.ureka.team3.utong_backend.auth.entity.Account;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_charge_history")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointChargeHistory {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "charged_amount", nullable = false)
    private Long chargedAmount;

    @Column(name = "fee_amount", nullable = false)
    private Long feeAmount;

    @Column(name = "final_amount", nullable = false)
    private Long finalAmount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
