package org.example.centrosnetapi.cabinas.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AulaResponseDTO {

    private Long id;
    private String numero;
    private String tipo;
    private String estado;

    private String alumnoNombre;
    private LocalDateTime inicio;
    private LocalDateTime fin;
}