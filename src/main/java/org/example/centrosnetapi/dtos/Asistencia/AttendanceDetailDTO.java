package org.example.centrosnetapi.dtos.Asistencia;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.centrosnetapi.models.EstadoAsistencia;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class AttendanceDetailDTO {

    private Long sesionId;
    private LocalDate fecha;
    private EstadoAsistencia estado;
}