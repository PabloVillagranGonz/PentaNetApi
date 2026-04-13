package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Asistencia.AsistenciaResponseDTO;
import org.example.centrosnetapi.dtos.Asistencia.AttendanceDTO;
import org.example.centrosnetapi.dtos.Asistencia.AttendanceDetailDTO;
import org.example.centrosnetapi.dtos.Asistencia.AttendanceSummaryDTO;
import org.example.centrosnetapi.services.AsistenciaService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/asistencia")
@RequiredArgsConstructor
@CrossOrigin
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    // =========================
    // 💾 GUARDAR
    // =========================
    @PostMapping
    public void save(@RequestBody AttendanceDTO dto) {
        asistenciaService.save(dto);
    }

    // =========================
    // 📊 OBTENER POR SESIÓN
    // =========================
    @GetMapping("/sesion/{sesionId}")
    public List<AsistenciaResponseDTO> getBySesion(
            @PathVariable Long sesionId,
            @RequestParam(required = false) String fecha
    ) {
        LocalDate f = fecha != null ? LocalDate.parse(fecha) : LocalDate.now();
        return asistenciaService.getBySesionAndFecha(sesionId, f);
    }

    @GetMapping("/resumen/{alumnoId}")
    public List<AttendanceSummaryDTO> getResumenAlumno(
            @PathVariable Long alumnoId
    ) {
        return asistenciaService.getResumenByAlumno(alumnoId);
    }

    @GetMapping("/student/{alumnoId}/subject/{asignaturaId}")
    public List<AttendanceDetailDTO> getDetailByAlumnoAndAsignatura(
            @PathVariable Long alumnoId,
            @PathVariable Long asignaturaId
    ) {
        return asistenciaService.getDetailByAlumnoAndAsignatura(alumnoId, asignaturaId);
    }
}