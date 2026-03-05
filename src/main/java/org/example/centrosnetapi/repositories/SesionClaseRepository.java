package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.SesionClase;
import org.example.centrosnetapi.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface SesionClaseRepository extends JpaRepository<SesionClase, Long> {

    // ================= CONSULTAS BÁSICAS =================

    List<SesionClase> findByCurso_IdOrderByDiaSemanaAscHoraInicioAsc(Long cursoId);

    List<SesionClase> findByProfesorIdOrderByDiaSemanaAscHoraInicioAsc(Long profesorId);


    @Query("""
    SELECT COUNT(s) > 0
    FROM SesionClase s
    WHERE s.alumno.id = :alumnoId
      AND s.diaSemana = :diaSemana
      AND s.horaInicio < :horaFin
      AND s.horaFin > :horaInicio
""")
    boolean existsConflictingSessionForAlumno(
            @Param("alumnoId") Long alumnoId,
            @Param("diaSemana") Integer diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

    // ================= SOLAPAMIENTO ESPACIO =================

    @Query("""
        SELECT COUNT(s) > 0
        FROM SesionClase s
        WHERE s.espacio.id = :espacioId
          AND s.diaSemana = :diaSemana
          AND s.horaInicio < :horaFin
          AND s.horaFin > :horaInicio
    """)
    boolean existsConflictingSessionForEspacio(
            @Param("espacioId") Long espacioId,
            @Param("diaSemana") Integer diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

    @Query("""
    SELECT s FROM SesionClase s
    WHERE s.curso.id = :cursoId
    AND (
        s.alumno IS NULL
        OR s.alumno.id = :alumnoId
    )
    ORDER BY s.diaSemana ASC, s.horaInicio ASC
""")
    List<SesionClase> findScheduleForStudent(
            @Param("cursoId") Long cursoId,
            @Param("alumnoId") Long alumnoId
    );

    // ================= SOLAPAMIENTO PROFESOR =================

    @Query("""
        SELECT COUNT(s) > 0
        FROM SesionClase s
        WHERE s.profesor.id = :profesorId
          AND s.diaSemana = :diaSemana
          AND s.horaInicio < :horaFin
          AND s.horaFin > :horaInicio
    """)
    boolean existsConflictingSessionForProfesor(
            @Param("profesorId") Long profesorId,
            @Param("diaSemana") Integer diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );
    // 📚 Todas las sesiones de un curso
    List<SesionClase> findByCursoId(Long cursoId);


    @Query("""
       SELECT DISTINCT s.alumno
       FROM SesionClase s
       WHERE s.profesor.id = :teacherId
       AND s.alumno IS NOT NULL
       """)
    List<Usuario> findStudentsForTeacher(@Param("teacherId") Long teacherId);
}