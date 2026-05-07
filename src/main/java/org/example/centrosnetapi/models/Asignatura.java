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
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE asignaturas SET activo = false WHERE id=?")
@SQLRestriction("activo = true")
@Table(name = "asignaturas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Asignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ================= RELACIÓN =================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "centro_id", nullable = false)
    @JsonIgnoreProperties("asignaturas")
    private Centro centro;

    // ================= DATOS =================

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Builder.Default
    @Column(name = "duracion_minutos")
    private Integer duracionMinutos = 60;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAsignatura tipo;

    @Builder.Default
    @Column(name = "activo")
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @LastModifiedDate
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    // ================= RELACIONES INVERSAS =================

    @OneToMany(mappedBy = "asignatura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CriterioEvaluacion> criterios;

    @OneToMany(mappedBy = "asignatura", fetch = FetchType.LAZY)
    private List<AsignacionDocente> asignacionesDocentes = new ArrayList<>();

    @OneToMany(mappedBy = "asignatura", fetch = FetchType.LAZY)
    private List<SesionClase> sesiones = new ArrayList<>();

    @OneToMany(mappedBy = "asignatura", fetch = FetchType.LAZY)
    private List<GrupoMensajes> gruposMensajes = new ArrayList<>();

    @OneToMany(mappedBy = "asignatura", fetch = FetchType.LAZY)
    private List<AsignaturaCurso> cursos = new ArrayList<>();
}