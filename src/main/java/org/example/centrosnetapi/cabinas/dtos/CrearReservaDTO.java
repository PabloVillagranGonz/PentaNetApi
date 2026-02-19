package org.example.centrosnetapi.cabinas.dtos;

import lombok.Data;

@Data
public class CrearReservaDTO {
    private Integer usuarioId;
    private Long aulaId;
    private int duracion;
}