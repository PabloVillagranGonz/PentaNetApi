package org.example.centrosnetapi.services;

import org.example.centrosnetapi.dtos.Calificacion.AlumnoEvaluacionDTO;
import org.example.centrosnetapi.dtos.Calificacion.GuardarNotaRequest;
import org.example.centrosnetapi.dtos.Calificacion.NotaDetalleDTO;
import org.example.centrosnetapi.dtos.Calificacion.ResumenAsignaturaAlumnoDTO;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CalificacionService {

    @Autowired
    private CalificacionRepository calificacionRepository;

    @Autowired
    private CriterioEvaluacionRepository criterioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AsignaturaRepository asignaturaRepository;

    @Autowired
    private CursoRepository cursoRepository;

    // Cambiamos el repositorio para usar el de sesiones (el mismo que usas en asistencia)
    @Autowired
    private SesionClaseRepository sesionClaseRepository;

    public List<ResumenAsignaturaAlumnoDTO> obtenerNotasResumenAlumno(Long alumnoId) {
        // 1. Buscamos al alumno para saber quién es y su curso
        Usuario alumno = usuarioRepository.findById(alumnoId)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));

        if (alumno.getCurso() == null) return List.of();

        // 2. 🔥 CLAVE: Buscamos las asignaturas en las SESIONES de su curso
        // Esto garantiza que vea lo mismo que en Asistencia y lo mismo que ve el Profesor
        List<SesionClase> sesiones =
                sesionClaseRepository.findByCursoId(alumno.getCurso().getId());

        // 3. Extraemos las asignaturas únicas de esas sesiones
        List<Asignatura> asignaturasUnicas = sesiones.stream()
                .map(s -> s.getAsignatura())
                .distinct()
                .toList();

        List<ResumenAsignaturaAlumnoDTO> resumen = new ArrayList<>();

        // 4. Calculamos las medias sobre esas asignaturas del horario
        for (Asignatura asig : asignaturasUnicas) {
            List<CriterioEvaluacion> criterios = criterioRepository.findByAsignaturaIdAndCursoId(asig.getId(), alumno.getCurso().getId());

            BigDecimal sumaPonderada = BigDecimal.ZERO;
            BigDecimal totalPesoEvaluado = BigDecimal.ZERO;

            for (CriterioEvaluacion criterio : criterios) {
                Optional<Calificacion> calOpt = calificacionRepository.findByCriterioIdAndAlumnoId(criterio.getId(), alumnoId);
                if (calOpt.isPresent()) {
                    BigDecimal nota = calOpt.get().getNota();
                    sumaPonderada = sumaPonderada.add(nota.multiply(criterio.getPeso()));
                    totalPesoEvaluado = totalPesoEvaluado.add(criterio.getPeso());
                }
            }

            BigDecimal media = BigDecimal.ZERO;
            if (totalPesoEvaluado.compareTo(BigDecimal.ZERO) > 0) {
                media = sumaPonderada.divide(totalPesoEvaluado, 2, RoundingMode.HALF_UP);
            }

            resumen.add(ResumenAsignaturaAlumnoDTO.builder()
                    .asignaturaId(asig.getId())
                    .asignaturaNombre(asig.getNombre())
                    .notaMedia(media)
                    .cursoId(alumno.getCurso().getId())
                    .build());
        }

        return resumen;
    }

    // Obtener la tabla de evaluación para el profesor
    public List<AlumnoEvaluacionDTO> obtenerEvaluacionGrupo(Long asignaturaId, Long cursoId) {
        List<Usuario> alumnos = usuarioRepository.findByRolAndCursoId(Rol.ALUMNO, cursoId);
        List<CriterioEvaluacion> criterios = criterioRepository.findByAsignaturaIdAndCursoId(asignaturaId, cursoId);
        List<AlumnoEvaluacionDTO> respuesta = new ArrayList<>();

        for (Usuario alumno : alumnos) {
            List<NotaDetalleDTO> detalles = new ArrayList<>();
            BigDecimal sumaPonderada = BigDecimal.ZERO;
            BigDecimal totalPesoEvaluado = BigDecimal.ZERO;

            for (CriterioEvaluacion criterio : criterios) {
                Optional<Calificacion> calOpt = calificacionRepository.findByCriterioIdAndAlumnoId(criterio.getId(), alumno.getId());
                BigDecimal notaActual = calOpt.map(Calificacion::getNota).orElse(null);

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

            BigDecimal mediaFinal = BigDecimal.ZERO;
            if (totalPesoEvaluado.compareTo(BigDecimal.ZERO) > 0) {
                mediaFinal = sumaPonderada.divide(totalPesoEvaluado, 2, RoundingMode.HALF_UP);
            }

            respuesta.add(AlumnoEvaluacionDTO.builder()
                    .id(alumno.getId())
                    .nombre(alumno.getNombre())
                    .apellidos(alumno.getApellidos())
                    .detalleNotas(detalles)
                    .media(mediaFinal)
                    .build());
        }

        return respuesta;
    }

    // Guardar o actualizar una nota
    @Transactional
    public void guardarNota(GuardarNotaRequest request) {
        Optional<Calificacion> existente = calificacionRepository
                .findByCriterioIdAndAlumnoId(request.getCriterioId(), request.getAlumnoId());

        Calificacion calificacion;
        if (existente.isPresent()) {
            calificacion = existente.get();
            calificacion.setNota(request.getNota());
            calificacion.setComentarios(request.getComentarios());
        } else {
            CriterioEvaluacion criterio = criterioRepository.findById(request.getCriterioId())
                    .orElseThrow(() -> new RuntimeException("Criterio no encontrado"));
            Usuario alumno = usuarioRepository.findById(request.getAlumnoId())
                    .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));

            calificacion = new Calificacion();
            calificacion.setCriterio(criterio);
            calificacion.setAlumno(alumno);
            calificacion.setNota(request.getNota());
            calificacion.setComentarios(request.getComentarios());
        }

        calificacionRepository.save(calificacion);
    }

    // 👇 NUEVO MÉTODO: Crear un criterio
    @Transactional
    public void crearCriterio(Long asignaturaId, Long cursoId, String nombre, BigDecimal peso) {
        Asignatura asignatura = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        CriterioEvaluacion nuevoCriterio = new CriterioEvaluacion();
        nuevoCriterio.setAsignatura(asignatura);
        nuevoCriterio.setCurso(curso);
        nuevoCriterio.setNombre(nombre);
        nuevoCriterio.setPeso(peso);

        criterioRepository.save(nuevoCriterio);
    }
}