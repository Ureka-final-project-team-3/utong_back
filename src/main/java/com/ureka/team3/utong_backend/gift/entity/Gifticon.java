package com.ureka.team3.utong_backend.gift.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gifticon")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gifticon {

    @Id
    @Column(length = 36)
    private String id;

    private Long price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String name;
}
