package org.example.centrosnetapi.cabinas.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AulaDisponibilidadDTO {

    private Integer numero;
    private String estado;
    private LocalDateTime fin;
}