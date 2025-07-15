package com.ureka.team3.utong_backend.line.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id", nullable = false)
    private Line line;

    @Column(name = "data_code", length = 3)
    private String dataCode;

    private Long remaining;

    private Long purchased;

    private Long sell;

    @Column(name = "month")
    private LocalDate month;
}


