package org.example.centrosnetapi.dtos.Espacio;

import lombok.*;
import org.example.centrosnetapi.models.TipoEspacio;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspacioRequestDTO {

    private String nombre;
    private TipoEspacio tipo;
    private Integer capacidad;
    private Long centroId;
}