package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Asistencia.AsistenciaResponseDTO;
import org.example.centrosnetapi.dtos.Asistencia.AttendanceDTO;
import org.example.centrosnetapi.dtos.Asistencia.AttendanceDetailDTO;
import org.example.centrosnetapi.dtos.Asistencia.AttendanceSummaryDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.AsistenciaRepository;
import org.example.centrosnetapi.repositories.SesionClaseRepository;
import org.example.centrosnetapi.repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final SesionClaseRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;

    // ============================================================
    // MÉTODOS PÚBLICOS
    // ============================================================

    @Transactional // 🔥 Obligatorio porque hacemos varias operaciones de escritura
    public void save(AttendanceDTO dto) {
        validarDto(dto);

        SesionClase sesion = buscarSesion(dto.getSesionId());
        LocalDate fecha = dto.getFecha() != null ? dto.getFecha() : LocalDate.now();

        List<Asistencia> existentes = asistenciaRepository.findBySesionIdAndFecha(dto.getSesionId(), fecha);

        // 🔥 OPTIMIZACIÓN: Traemos a todos los alumnos en 1 sola consulta
        Map<Long, Usuario> alumnosMap = obtenerAlumnosEnBloque(dto.getAsistencias());
        List<Asistencia> asistenciasAGuardar = new ArrayList<>();

        for (AttendanceDTO.Item item : dto.getAsistencias()) {
            Usuario alumno = alumnosMap.get(item.getAlumnoId());
            if (alumno == null) {
                throw new ApiException("USER_NOT_FOUND_ID_" + item.getAlumnoId(), HttpStatus.NOT_FOUND);
            }

            Asistencia asistencia = buscarOInstanciar(existentes, item.getAlumnoId(), sesion, alumno, fecha);
            asistencia.setEstado(item.getPresente() ? EstadoAsistencia.PRESENTE : EstadoAsistencia.AUSENTE);

            asistenciasAGuardar.add(asistencia);
        }

        // 🔥 OPTIMIZACIÓN: Guardamos todo de golpe (1 sola consulta de inserción/actualización)
        asistenciaRepository.saveAll(asistenciasAGuardar);
    }

    public List<AsistenciaResponseDTO> getBySesionAndFecha(Long sesionId, LocalDate fecha) {
        return asistenciaRepository.findBySesionIdAndFecha(sesionId, fecha)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<AttendanceSummaryDTO> getResumenByAlumno(Long alumnoId) {
        return asistenciaRepository.getAttendanceSummaryByAlumno(alumnoId);
    }

    public List<AttendanceDetailDTO> getDetailByAlumnoAndAsignatura(Long alumnoId, Long asignaturaId) {
        return asistenciaRepository.findDetailByAlumnoAndAsignatura(alumnoId, asignaturaId);
    }

    // ============================================================
    // MÉTODOS PRIVADOS
    // ============================================================

    private void validarDto(AttendanceDTO dto) {
        if (dto.getAsistencias() == null || dto.getAsistencias().isEmpty()) {
            throw new ApiException("EMPTY_ATTENDANCE", HttpStatus.BAD_REQUEST);
        }
    }

    private SesionClase buscarSesion(Long sesionId) {
        return sesionRepository.findById(sesionId)
                .orElseThrow(() -> new ApiException("SESSION_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private Map<Long, Usuario> obtenerAlumnosEnBloque(List<AttendanceDTO.Item> items) {
        // Sacamos todos los IDs
        List<Long> alumnoIds = items.stream().map(AttendanceDTO.Item::getAlumnoId).toList();

        // Hacemos un findAllById (1 sola consulta) y lo convertimos a un Mapa para buscar rápido
        return usuarioRepository.findAllById(alumnoIds).stream()
                .collect(Collectors.toMap(Usuario::getId, Function.identity()));
    }

    private Asistencia buscarOInstanciar(List<Asistencia> existentes, Long alumnoId, SesionClase sesion, Usuario alumno, LocalDate fecha) {
        return existentes.stream()
                .filter(a -> a.getAlumno().getId().equals(alumnoId))
                .findFirst()
                .orElseGet(() -> crearNuevaAsistencia(sesion, alumno, fecha));
    }

    private Asistencia crearNuevaAsistencia(SesionClase sesion, Usuario alumno, LocalDate fecha) {
        Asistencia asistencia = new Asistencia();
        asistencia.setSesion(sesion);
        asistencia.setAlumno(alumno);
        asistencia.setFecha(fecha);
        return asistencia;
    }

    private AsistenciaResponseDTO toResponseDTO(Asistencia a) {
        return AsistenciaResponseDTO.builder()
                .alumnoId(a.getAlumno().getId())
                .estado(a.getEstado().name())
                .build();
    }
}