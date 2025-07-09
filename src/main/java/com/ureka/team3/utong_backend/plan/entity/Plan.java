package com.ureka.team3.utong_backend.plan.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "plan")
@Getter
@Setter
@NoArgsConstructor
public class Plan {
    @Id
    private String id;

    private String name;

    private Long data;
}
