package org.example.centrosnetapi.dtos.Reserva;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearReservaDTO {

    private Long usuarioId;
    private Long aulaId;
    private Integer duracion;
}