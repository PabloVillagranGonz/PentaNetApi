package org.example.centrosnetapi.dtos.SesionClase;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionClaseResponseDTO {

    private Long id;

    private Long cursoId;
    private String cursoNombre;

    private Long asignaturaId;
    private String asignaturaNombre;
    private String tipoAsignatura;

    private Long profesorId;
    private String profesorNombreCompleto;

    private Long alumnoId;
    private String alumnoNombreCompleto;

    private Long espacioId;
    private String espacioNombre;

    private Integer diaSemana;

    private LocalTime horaInicio;
    private LocalTime horaFin;

    private String notas;
}