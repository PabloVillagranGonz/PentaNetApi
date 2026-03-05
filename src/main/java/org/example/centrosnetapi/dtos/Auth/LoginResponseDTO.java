package org.example.centrosnetapi.dtos.Auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDTO {

    private Long id;
    private String nombre;
    private String apellidos;
    private String email;
    private String rol;
    private String token;

    private Long centroId;
    private String centroNombre;

    private Long cursoId;
    private String cursoNombre;

    private Long instrumentoId;
    private String instrumentoNombre;
}