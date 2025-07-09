package com.ureka.team3.utong_backend.line.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "line_data")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineData {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "data_code", length = 3)
    private String dataCode;

    private Long remining;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
}


