package org.example.centrosnetapi.dtos.Reserva;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponseDTO {

    private Long id;

    private Long usuarioId;
    private String usuarioNombre;

    private Long aulaId;
    private String aulaNombre;

    private LocalDateTime inicio;
    private LocalDateTime fin;
    private LocalDateTime finReal;

    private Boolean finalizadaAntes;
}