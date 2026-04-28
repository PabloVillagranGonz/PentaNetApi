package org.example.centrosnetapi.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
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

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos = 60;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAsignatura tipo;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", insertable = false)
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