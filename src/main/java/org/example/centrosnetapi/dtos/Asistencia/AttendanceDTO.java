package org.example.centrosnetapi.dtos.Asistencia;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AttendanceDTO {

    private Long sesionId;
    private LocalDate fecha;
    private List<Item> asistencias;

    @Data
    public static class Item {
        private Long alumnoId;
        private Boolean presente;
    }
}