package org.example.centrosnetapi.dtos.Calificacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoEvaluacionDTO {
    private Long id;
    private String nombre;
    private String apellidos;
    private List<NotaDetalleDTO> detalleNotas;
    private BigDecimal media;
}