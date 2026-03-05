package org.example.centrosnetapi.dtos.SesionClase;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionClaseRequestDTO {

    @NotNull
    private Long cursoId;

    @NotNull
    private Long asignaturaId;

    @NotNull
    private Long profesorId;

    @NotNull
    private Long espacioId;

    /**
     * Solo obligatorio si la asignatura es INDIVIDUAL
     */
    private Long alumnoId;

    @NotNull
    @Min(1)
    @Max(7)
    private Integer diaSemana;

    @NotNull
    private LocalTime horaInicio;

    @NotNull
    private LocalTime horaFin;

    private String notas;
}