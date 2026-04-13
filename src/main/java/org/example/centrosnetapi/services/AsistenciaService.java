package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Asistencia.AsistenciaResponseDTO;
import org.example.centrosnetapi.dtos.Asistencia.AttendanceDTO;
import org.example.centrosnetapi.dtos.Asistencia.AttendanceDetailDTO;
import org.example.centrosnetapi.dtos.Asistencia.AttendanceSummaryDTO;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.AsistenciaRepository;
import org.example.centrosnetapi.repositories.SesionClaseRepository;
import org.example.centrosnetapi.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final SesionClaseRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;

    // =========================
    // 💾 GUARDAR / ACTUALIZAR
    // =========================
    public void save(AttendanceDTO dto) {

        if (dto.getAsistencias() == null || dto.getAsistencias().isEmpty()) {
            throw new RuntimeException("EMPTY_ATTENDANCE");
        }

        SesionClase sesion = sesionRepository.findById(dto.getSesionId())
                .orElseThrow(() -> new RuntimeException("SESSION_NOT_FOUND"));

        LocalDate fecha = dto.getFecha() != null ? dto.getFecha() : LocalDate.now();

        // 🔥 Traemos existentes UNA VEZ
        List<Asistencia> existentes =
                asistenciaRepository.findBySesionIdAndFecha(dto.getSesionId(), fecha);

        for (AttendanceDTO.Item item : dto.getAsistencias()) {

            Usuario alumno = usuarioRepository.findById(item.getAlumnoId())
                    .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

            // 🔍 buscar si ya existe
            Asistencia asistencia = existentes.stream()
                    .filter(a -> a.getAlumno().getId().equals(item.getAlumnoId()))
                    .findFirst()
                    .orElse(null);

            if (asistencia == null) {
                asistencia = new Asistencia();
                asistencia.setSesion(sesion);
                asistencia.setAlumno(alumno);
                asistencia.setFecha(fecha);
            }

            // 👉 boolean → enum
            asistencia.setEstado(
                    item.getPresente()
                            ? EstadoAsistencia.PRESENTE
                            : EstadoAsistencia.AUSENTE
            );

            asistenciaRepository.save(asistencia);
        }
    }

    public List<AsistenciaResponseDTO> getBySesionAndFecha(Long sesionId, LocalDate fecha) {

        return asistenciaRepository.findBySesionIdAndFecha(sesionId, fecha)
                .stream()
                .map(a -> AsistenciaResponseDTO.builder()
                        .alumnoId(a.getAlumno().getId())
                        .estado(a.getEstado().name())
                        .build())
                .toList();
    }

    public List<AttendanceSummaryDTO> getResumenByAlumno(Long alumnoId) {
        return asistenciaRepository.getAttendanceSummaryByAlumno(alumnoId);
    }

    public List<AttendanceDetailDTO> getDetailByAlumnoAndAsignatura(Long alumnoId, Long asignaturaId) {
        return asistenciaRepository.findDetailByAlumnoAndAsignatura(alumnoId, asignaturaId);
    }
}