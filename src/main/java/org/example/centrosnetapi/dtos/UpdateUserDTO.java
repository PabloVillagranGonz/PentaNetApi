package org.example.centrosnetapi.dtos;

import lombok.Data;
import org.example.centrosnetapi.models.Role;

@Data
public class UpdateUserDTO {

    private String nombre;
    private String apellidos;
    private String email;
    private Role role;
    private Long center_id;
    private Long instrument_id;
    private Long course_id;
    private String phone;
    private String dni;
}
