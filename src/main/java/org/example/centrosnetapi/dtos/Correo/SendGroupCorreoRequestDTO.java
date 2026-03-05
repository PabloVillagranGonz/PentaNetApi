package org.example.centrosnetapi.dtos.Correo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendGroupCorreoRequestDTO {

    @NotNull(message = "La asignatura es obligatoria")
    private Long asignaturaId;

    @NotBlank(message = "El asunto es obligatorio")
    private String asunto;

    @NotBlank(message = "El cuerpo del mensaje es obligatorio")
    private String cuerpo;
}