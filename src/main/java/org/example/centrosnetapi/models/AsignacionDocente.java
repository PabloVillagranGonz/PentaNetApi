package org.example.centrosnetapi.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
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

    @org.springframework.data.annotation.CreatedDate
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @org.springframework.data.annotation.LastModifiedDate
    @Column(name = "actualizado_en")
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