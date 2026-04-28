package org.example.centrosnetapi.dtos.Calificacion;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CrearCriterioRequest {
    private Long asignaturaId;
    private Long cursoId;
    private String nombre;
    private BigDecimal peso;
}
