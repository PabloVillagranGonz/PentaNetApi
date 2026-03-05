package org.example.centrosnetapi.dtos.Curso;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponseDTO {

    private Long id;

    private String nombre;
    private Integer anio;
    private String notas;

    private Long centroId;
    private String centroNombre;
}