package org.example.centrosnetapi.dtos.Calificacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaDetalleDTO {
    private Long criterioId;
    private String nombreCriterio;
    private BigDecimal peso;
    private BigDecimal nota; // Será null si el profesor aún no ha puesto nota
}