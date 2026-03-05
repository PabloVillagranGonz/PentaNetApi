package org.example.centrosnetapi.dtos.Espacio;

import lombok.*;
import org.example.centrosnetapi.models.TipoEspacio;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspacioResponseDTO {

    private Long id;
    private String nombre;
    private TipoEspacio tipo;
    private Integer capacidad;
    private Long centroId;
    private Boolean ocupada;
    private String alumnoNombre;
    private LocalDateTime inicio;
    private LocalDateTime fin;
}