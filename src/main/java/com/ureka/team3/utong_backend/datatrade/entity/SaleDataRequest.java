package com.ureka.team3.utong_backend.datatrade.entity;

import com.ureka.team3.utong_backend.auth.entity.Account;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sale_data_request")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleDataRequest {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", foreignKey = @ForeignKey(name = "FK_account_TO_sale_data_request_1"))
    private Account account;

    private Long price;

    @Column(name = "data_code", length = 3)
    private String dataCode;

    private Long quantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "line_id")
    private String lineId;

    @Column
    private String status;

    @Column
    private Long remaining;

    @PrePersist
    public void initId() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.expiredAt == null) this.expiredAt = createdAt.plusDays(3);
//        if (this.expiredAt == null) this.expiredAt = createdAt.plusMinutes(10);
    }

    public void changeStatus(String status) {
        this.status = status;
    }

    public void changeRemaining(Long remaining) {
        this.remaining = remaining;
    }

    public void subtractRemain(long quantity) {
        this.remaining -= quantity;
        if (this.remaining == 0) {
            this.status = "001";
            return;
        }

        if (!this.remaining.equals(this.quantity) && this.remaining > 0) {
            this.status = "002";
        }
    }

    public boolean isOwner(String id) {
        return this.account.isMyId(id);
    }

    public boolean isStatus(String code) {
        return this.status.equals(code);
    }
}
