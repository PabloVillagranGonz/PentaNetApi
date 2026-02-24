package org.example.centrosnetapi.cabinas.models;

import jakarta.persistence.*;
import lombok.Data;
import org.example.centrosnetapi.models.Center;
import org.example.centrosnetapi.models.Instrument;

@Data
@Entity
@Table(name = "aulas")
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String numero;

    private String tipo;

    @ManyToOne
    @JoinColumn(name = "center_id")
    private Center center;

}