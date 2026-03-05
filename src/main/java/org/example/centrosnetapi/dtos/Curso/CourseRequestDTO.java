package org.example.centrosnetapi.dtos.Curso;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    private Integer anio;

    @Size(max = 255)
    private String notas;

    @NotNull(message = "El centro es obligatorio")
    private Long centroId;
}