package org.example.centrosnetapi.dtos.Correo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendCorreoRequestDTO {

    @NotNull(message = "El destinatario es obligatorio")
    private Long destinatarioId;

    @NotBlank(message = "El asunto es obligatorio")
    private String asunto;

    @NotBlank(message = "El cuerpo del mensaje es obligatorio")
    private String cuerpo;
}