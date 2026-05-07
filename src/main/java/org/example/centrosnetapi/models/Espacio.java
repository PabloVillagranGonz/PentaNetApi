package org.example.centrosnetapi.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE espacios SET activo = false WHERE id=?")
@SQLRestriction("activo = true")
@Table(name = "espacios", uniqueConstraints = @UniqueConstraint(columnNames = { "centro_id", "nombre" }))
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

    @Builder.Default
    @Column(name = "activo")
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @LastModifiedDate
    @Column(name = "actualizado_en")
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