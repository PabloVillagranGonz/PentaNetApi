package org.example.centrosnetapi.dtos.Calificacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class ResumenAsignaturaAlumnoDTO {
    private Long asignaturaId;
    private String asignaturaNombre;
    private BigDecimal notaMedia;
    private Long cursoId;
    private Boolean publicadas;
}