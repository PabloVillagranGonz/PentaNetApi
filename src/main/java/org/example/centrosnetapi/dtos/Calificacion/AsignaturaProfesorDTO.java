package org.example.centrosnetapi.dtos.Calificacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignaturaProfesorDTO {
    private Long id;
    private String nombre;
    private Long cursoId;
    private String cursoNombre;
}
