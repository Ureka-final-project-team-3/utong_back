package com.ureka.team3.utong_backend.chart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @Column(name = "sale_data_request_id", length = 36)
    private String saleDataRequestId;
    
    @Column(name = "buy_data_request_id", length = 36)
    private String buyDataRequestId;
    
    @Column(name = "status_code", length = 3)
    private String statusCode;
    
    @Column(name = "is_reserved")
    private Boolean isReserved;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
}