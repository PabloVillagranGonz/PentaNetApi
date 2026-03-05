package org.example.centrosnetapi.dtos.Correo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorreoResponseDTO {

    private Long id;

    private String asunto;
    private String cuerpo;

    private Long emisorId;
    private String emisorNombre;

    private Long destinatarioId;
    private String destinatarioNombre;

    private Boolean leido;
    private Boolean archivado;

    private LocalDateTime fechaEnvio;
}