package org.example.centrosnetapi.dtos.Usuario;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherResponseDTO {

    private Long id;

    private String nombre;
    private String apellidos;
    private String email;

    private Long centroId;
    private String centroNombre;
}