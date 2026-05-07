package org.example.centrosnetapi.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "usuarios_mensajes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"mensaje_id", "usuario_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UsuarioMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ================= RELACIONES =================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mensaje_id", nullable = false)
    private Mensaje mensaje;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // ================= ESTADO =================

    private Boolean leido = false;

    private Boolean eliminado = false;

    private Boolean archivado = false;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @Column(name = "fecha_eliminacion")
    private LocalDateTime fechaEliminacion;

    @CreatedDate
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    // ================= VALIDACIÓN DOMINIO =================

    @PrePersist
    @PreUpdate
    private void validarEstado() {

        if (Boolean.TRUE.equals(leido) && fechaLectura == null) {
            fechaLectura = LocalDateTime.now();
        }

        if (Boolean.TRUE.equals(eliminado) && fechaEliminacion == null) {
            fechaEliminacion = LocalDateTime.now();
        }
    }
}