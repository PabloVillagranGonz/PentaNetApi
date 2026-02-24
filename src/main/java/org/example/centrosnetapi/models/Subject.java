package org.example.centrosnetapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subjects")
@Getter
@Setter
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    // =========================
    // RELACIONES
    // =========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id")
    private Center center;
}