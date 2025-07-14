package com.ureka.team3.utong_backend.line.entity;

import com.ureka.team3.utong_backend.auth.entity.Line;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    public void purchaseData(Long amount){
        this.purchased += amount;
    }

    public void saleData(Long amount){
        this.remaining -= amount;
        this.sell += amount;
    }
}


