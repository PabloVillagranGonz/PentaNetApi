package org.example.centrosnetapi.dtos.Asignatura;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "La duración es obligatoria")
    @Positive(message = "La duración debe ser mayor que 0")
    private Integer duracionMinutos;

    @NotNull(message = "El centro es obligatorio")
    private Long centroId;

    @NotNull(message = "El tipo de asignatura es obligatorio")
    private String tipo; // COLECTIVA o INDIVIDUAL
}