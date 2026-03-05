package org.example.centrosnetapi.dtos.Usuario;

import lombok.*;
import org.example.centrosnetapi.models.Rol;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;

    private String nombre;
    private String apellidos;
    private String email;

    private Rol rol;

    private String telefono;
    private String dni;

    private Long centroId;
    private String centroNombre;

    private Long cursoId;
    private String cursoNombre;

    private Long instrumentoId;
    private String instrumentoNombre;
}