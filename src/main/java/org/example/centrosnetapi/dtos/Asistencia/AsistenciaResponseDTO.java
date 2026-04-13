package org.example.centrosnetapi.dtos.Asistencia;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsistenciaResponseDTO {

    private Long alumnoId;
    private String estado;
}