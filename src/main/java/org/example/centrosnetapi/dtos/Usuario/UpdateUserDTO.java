package org.example.centrosnetapi.dtos.Usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.centrosnetapi.models.Rol;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserDTO {

    @Size(max = 100)
    private String nombre;

    @Size(max = 150)
    private String apellidos;

    @Email(message = "Formato de email inválido")
    private String email;

    private String telefono;

    private String dni;

    private Rol rol;

    // ================= RELACIONES =================

    private Long centroId;

    private Long cursoId;

    private Long instrumentoId;
}