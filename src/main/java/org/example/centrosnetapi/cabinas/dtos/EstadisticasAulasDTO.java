package org.example.centrosnetapi.cabinas.dtos;

import lombok.*;

@Data
@Getter
@AllArgsConstructor
public class EstadisticasAulasDTO {

    private long total;
    private long libres;
    private long ocupadas;
    private long reservasHoy;
}