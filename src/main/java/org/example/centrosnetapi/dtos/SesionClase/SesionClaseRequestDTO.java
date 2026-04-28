package org.example.centrosnetapi.dtos.SesionClase;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFin;
    private String notas;
}