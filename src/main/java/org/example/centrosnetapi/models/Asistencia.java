package org.example.centrosnetapi.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "asistencia",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_asistencia",
                columnNames = {"sesion_id", "alumno_id", "fecha"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 RELACIONES
    @ManyToOne(optional = false)
    @JoinColumn(name = "sesion_id")
    private SesionClase sesion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alumno_id")
    private Usuario alumno;

    // 📅 día concreto
    @Column(nullable = false)
    private LocalDate fecha;

    // 📊 estado
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAsistencia estado;
}