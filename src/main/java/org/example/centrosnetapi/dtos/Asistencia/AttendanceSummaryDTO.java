package org.example.centrosnetapi.dtos.Asistencia;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttendanceSummaryDTO {
    private Long asignaturaId;
    private String asignatura;
    private Long faltas;
    private Long totalClases;
    private Double porcentajeAsistencia;
}