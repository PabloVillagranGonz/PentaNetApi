package org.example.centrosnetapi.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "espacios",
        uniqueConstraints = @UniqueConstraint(columnNames = {"centro_id", "nombre"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Espacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ================= RELACIÓN =================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "centro_id", nullable = false)
    @JsonIgnoreProperties("espacios")
    private Centro centro;

    // ================= DATOS =================

    @Column(nullable = false, length = 50)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEspacio tipo;

    private Integer capacidad = 1;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", insertable = false)
    private LocalDateTime actualizadoEn;

    // ================= VALIDACIÓN =================

    @PrePersist
    @PreUpdate
    private void validarCapacidad() {
        if (capacidad == null || capacidad < 1) {
            capacidad = 1;
        }
    }
}