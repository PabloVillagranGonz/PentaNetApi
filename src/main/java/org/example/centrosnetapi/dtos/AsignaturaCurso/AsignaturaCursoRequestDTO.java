package org.example.centrosnetapi.dtos.AsignaturaCurso;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignaturaCursoRequestDTO {

    private Long cursoId;
    private Long asignaturaId;

    // opcional si quieres control horario
    private BigDecimal horasSemanales;
}