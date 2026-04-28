package org.example.centrosnetapi.dtos.Calificacion;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GuardarNotaRequest {
    private Long criterioId;
    private Long alumnoId;
    private BigDecimal nota;
    private String comentarios;
}