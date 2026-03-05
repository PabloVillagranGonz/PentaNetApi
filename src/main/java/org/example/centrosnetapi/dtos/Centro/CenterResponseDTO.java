package org.example.centrosnetapi.dtos.Centro;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CenterResponseDTO {

    private Long id;

    private String nombre;
    private String telefono;
    private String email;
    private String website;
    private String horarioApertura;
    private String direccion;
    private String codigoPostal;
    private String ciudad;
}