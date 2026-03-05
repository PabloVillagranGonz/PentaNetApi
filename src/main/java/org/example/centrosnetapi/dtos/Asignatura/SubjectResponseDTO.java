package org.example.centrosnetapi.dtos.Asignatura;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubjectResponseDTO {

    private Long id;

    private String nombre;
    private String descripcion;
    private Integer duracionMinutos;

    private String tipo;

    private Long centroId;
}