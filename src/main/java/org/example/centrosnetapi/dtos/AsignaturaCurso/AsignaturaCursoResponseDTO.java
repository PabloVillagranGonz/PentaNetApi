package org.example.centrosnetapi.dtos.AsignaturaCurso;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignaturaCursoResponseDTO {

    private Long id;

    private Long cursoId;
    private String cursoNombre;

    private Long asignaturaId;
    private String asignaturaNombre;

    private BigDecimal horasSemanales;
}