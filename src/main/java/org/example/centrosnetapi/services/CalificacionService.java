package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Calificacion.AlumnoEvaluacionDTO;
import org.example.centrosnetapi.dtos.Calificacion.GuardarNotaRequest;
import org.example.centrosnetapi.dtos.Calificacion.NotaDetalleDTO;
import org.example.centrosnetapi.dtos.Calificacion.ResumenAsignaturaAlumnoDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // 🔥 Inyección limpia de dependencias (adiós a los múltiples @Autowired)
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final CriterioEvaluacionRepository criterioRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final CursoRepository cursoRepository;
    private final SesionClaseRepository sesionClaseRepository;

    // ============================================================
    // MÉTODOS PÚBLICOS (Lógica Orquestadora)
    // ============================================================

    public List<ResumenAsignaturaAlumnoDTO> obtenerNotasResumenAlumno(Long alumnoId) {
        Usuario alumno = buscarAlumno(alumnoId);

        if (alumno.getCurso() == null) return List.of();

        return obtenerAsignaturasDelCurso(alumno.getCurso().getId())
                .stream()
                .map(asignatura -> procesarResumenAsignatura(asignatura, alumno.getCurso().getId(), alumnoId))
                .toList();
    }

    public List<AlumnoEvaluacionDTO> obtenerEvaluacionGrupo(Long asignaturaId, Long cursoId) {
        List<Usuario> alumnos = usuarioRepository.findByRolAndCursoId(Rol.ALUMNO, cursoId);
        List<CriterioEvaluacion> criterios = criterioRepository.findByAsignaturaIdAndCursoId(asignaturaId, cursoId);

        return alumnos.stream()
                .map(alumno -> evaluarAlumnoCompleto(alumno, criterios))
                .toList();
    }

    @Transactional
    public void guardarNota(GuardarNotaRequest request) {
        calificacionRepository.findByCriterioIdAndAlumnoId(request.getCriterioId(), request.getAlumnoId())
                .ifPresentOrElse(
                        calificacion -> actualizarCalificacionExistente(calificacion, request),
                        () -> crearNuevaCalificacion(request)
                );
    }

    @Transactional
    public void crearCriterio(Long asignaturaId, Long cursoId, String nombre, BigDecimal peso) {
        Asignatura asignatura = buscarAsignatura(asignaturaId);
        Curso curso = buscarCurso(cursoId);

        CriterioEvaluacion nuevoCriterio = new CriterioEvaluacion();
        nuevoCriterio.setAsignatura(asignatura);
        nuevoCriterio.setCurso(curso);
        nuevoCriterio.setNombre(nombre);
        nuevoCriterio.setPeso(peso);

        criterioRepository.save(nuevoCriterio);
    }

    // ============================================================
    // MÉTODOS PRIVADOS (Cálculos y Validaciones)
    // ============================================================

    private ResumenAsignaturaAlumnoDTO procesarResumenAsignatura(Asignatura asignatura, Long cursoId, Long alumnoId) {
        List<CriterioEvaluacion> criterios = criterioRepository.findByAsignaturaIdAndCursoId(asignatura.getId(), cursoId);

        BigDecimal media = calcularMediaPonderada(criterios, alumnoId);

        return ResumenAsignaturaAlumnoDTO.builder()
                .asignaturaId(asignatura.getId())
                .asignaturaNombre(asignatura.getNombre())
                .notaMedia(media)
                .cursoId(cursoId)
                .build();
    }

    private AlumnoEvaluacionDTO evaluarAlumnoCompleto(Usuario alumno, List<CriterioEvaluacion> criterios) {
        List<NotaDetalleDTO> detalles = new ArrayList<>();
        BigDecimal sumaPonderada = BigDecimal.ZERO;
        BigDecimal totalPesoEvaluado = BigDecimal.ZERO;

        for (CriterioEvaluacion criterio : criterios) {
            BigDecimal notaActual = obtenerNotaActual(criterio.getId(), alumno.getId());

            detalles.add(NotaDetalleDTO.builder()
                    .criterioId(criterio.getId())
                    .nombreCriterio(criterio.getNombre())
                    .peso(criterio.getPeso())
                    .nota(notaActual)
                    .build());

            if (notaActual != null) {
                sumaPonderada = sumaPonderada.add(notaActual.multiply(criterio.getPeso()));
                totalPesoEvaluado = totalPesoEvaluado.add(criterio.getPeso());
            }
        }

        return AlumnoEvaluacionDTO.builder()
                .id(alumno.getId())
                .nombre(alumno.getNombre())
                .apellidos(alumno.getApellidos())
                .detalleNotas(detalles)
                .media(calcularMediaSegura(sumaPonderada, totalPesoEvaluado))
                .build();
    }

    // 🧮 Motor Matemático Aislado
    private BigDecimal calcularMediaPonderada(List<CriterioEvaluacion> criterios, Long alumnoId) {
        BigDecimal sumaPonderada = BigDecimal.ZERO;
        BigDecimal totalPesoEvaluado = BigDecimal.ZERO;

        for (CriterioEvaluacion criterio : criterios) {
            BigDecimal nota = obtenerNotaActual(criterio.getId(), alumnoId);
            if (nota != null) {
                sumaPonderada = sumaPonderada.add(nota.multiply(criterio.getPeso()));
                totalPesoEvaluado = totalPesoEvaluado.add(criterio.getPeso());
            }
        }
        return calcularMediaSegura(sumaPonderada, totalPesoEvaluado);
    }

    private BigDecimal calcularMediaSegura(BigDecimal sumaPonderada, BigDecimal totalPeso) {
        if (totalPeso.compareTo(BigDecimal.ZERO) > 0) {
            return sumaPonderada.divide(totalPeso, 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal obtenerNotaActual(Long criterioId, Long alumnoId) {
        return calificacionRepository.findByCriterioIdAndAlumnoId(criterioId, alumnoId)
                .map(Calificacion::getNota)
                .orElse(null);
    }

    // 💾 Ayudantes de Guardado (ifPresentOrElse)
    private void actualizarCalificacionExistente(Calificacion calificacion, GuardarNotaRequest request) {
        calificacion.setNota(request.getNota());
        calificacion.setComentarios(request.getComentarios());
        calificacionRepository.save(calificacion);
    }

    private void crearNuevaCalificacion(GuardarNotaRequest request) {
        CriterioEvaluacion criterio = criterioRepository.findById(request.getCriterioId())
                .orElseThrow(() -> new ApiException("CRITERIO_NOT_FOUND", HttpStatus.NOT_FOUND));
        Usuario alumno = buscarAlumno(request.getAlumnoId());

        Calificacion nuevaCalificacion = new Calificacion();
        nuevaCalificacion.setCriterio(criterio);
        nuevaCalificacion.setAlumno(alumno);
        nuevaCalificacion.setNota(request.getNota());
        nuevaCalificacion.setComentarios(request.getComentarios());

        calificacionRepository.save(nuevaCalificacion);
    }

    // 🔍 Buscadores y Helpers
    private List<Asignatura> obtenerAsignaturasDelCurso(Long cursoId) {
        return sesionClaseRepository.findByCursoId(cursoId).stream()
                .map(SesionClase::getAsignatura)
                .distinct()
                .toList();
    }

    private Usuario buscarAlumno(Long alumnoId) {
        return usuarioRepository.findById(alumnoId)
                .orElseThrow(() -> new ApiException("ALUMNO_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private Asignatura buscarAsignatura(Long asignaturaId) {
        return asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new ApiException("ASIGNATURA_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private Curso buscarCurso(Long cursoId) {
        return cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ApiException("CURSO_NOT_FOUND", HttpStatus.NOT_FOUND));
    }
}