package org.example.centrosnetapi.dtos.Centro;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CenterRequestDTO {

    private String nombre;
    private String telefono;
    private String email;
    private String website;
    private String horarioApertura;
    private String direccion;
    private String codigoPostal;
    private String ciudad;
}