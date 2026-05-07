package org.example.centrosnetapi.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE centros SET activo = false WHERE id=?")
@SQLRestriction("activo = true")
@Table(name = "centros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Centro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String telefono;

    private String email;

    private String website;

    @Column(name = "horario_apertura")
    private String horarioApertura;

    private String direccion;

    @Column(name = "codigo_postal")
    private String codigoPostal;

    private String ciudad;

    @Builder.Default
    @Column(name = "activo")
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @LastModifiedDate
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    // ================= RELACIONES =================

    @Builder.Default
    @OneToMany(mappedBy = "centro",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Usuario> usuarios = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "centro",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Curso> cursos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "centro",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Espacio> espacios = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "centro",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Asignatura> asignaturas = new ArrayList<>();
}