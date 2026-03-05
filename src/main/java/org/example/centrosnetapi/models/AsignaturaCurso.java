package org.example.centrosnetapi.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "asignaturas_cursos",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"curso_id", "asignatura_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignaturaCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= RELACIONES =================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asignatura_id", nullable = false)
    private Asignatura asignatura;

    // ================= DATOS EXTRA =================

    @Column(name = "horas_semanales", precision = 5, scale = 2)
    private BigDecimal horasSemanales = BigDecimal.ZERO;
}