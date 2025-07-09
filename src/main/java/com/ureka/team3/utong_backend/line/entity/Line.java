package com.ureka.team3.utong_backend.line.entity;

import com.ureka.team3.utong_backend.auth.entity.User;
import com.ureka.team3.utong_backend.plan.entity.Plan;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "line", uniqueConstraints = @UniqueConstraint(name = "uk_phone_number", columnNames = "phone_number"))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Line {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "country_code")
    private Integer countryCode;
}
