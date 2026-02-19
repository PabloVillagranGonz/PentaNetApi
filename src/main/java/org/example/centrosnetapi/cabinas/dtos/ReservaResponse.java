package org.example.centrosnetapi.cabinas.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReservaResponse {

    private Long id;
    private Integer usuarioId;
    private String usuarioNombre;
    private Long aulaId;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private LocalDateTime finReal;
    private boolean finalizadaAntes;

}