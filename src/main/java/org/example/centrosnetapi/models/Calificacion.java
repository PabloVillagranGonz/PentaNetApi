package org.example.centrosnetapi.models;

import jakarta.persistence.*;
import lombok.Data; // Borra si no usas Lombok
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "calificaciones")
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterio_id", nullable = false)
    private CriterioEvaluacion criterio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Usuario alumno;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal nota;

    @Column(columnDefinition = "TEXT")
    private String comentarios;

    @Column(name = "fecha_evaluacion")
    private LocalDate fechaEvaluacion;

    @CreatedDate
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @LastModifiedDate
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    public void prePersist() {
        if (fechaEvaluacion == null) {
            fechaEvaluacion = LocalDate.now();
        }
    }
}