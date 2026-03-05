package org.example.centrosnetapi.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "asignaciones_docentes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "profesor_id",
                        "asignatura_id",
                        "curso_id"
                })
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AsignacionDocente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ================= RELACIONES =================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asignatura_id", nullable = false)
    private Asignatura asignatura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profesor_id", nullable = false)
    private Usuario profesor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    // ================= DATOS =================

    @Column(name = "rol_docente")
    private String rolDocente = "Titular";

    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", insertable = false)
    private LocalDateTime actualizadoEn;

    // ================= VALIDACIÓN DOMINIO =================

    @PrePersist
    @PreUpdate
    private void validarProfesor() {
        if (profesor != null && profesor.getRol() != Rol.PROFESOR) {
            throw new IllegalArgumentException("El usuario asignado no tiene rol de PROFESOR");
        }
    }
}