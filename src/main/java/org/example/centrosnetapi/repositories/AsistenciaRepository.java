package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.dtos.Asistencia.AttendanceDetailDTO;
import org.example.centrosnetapi.dtos.Asistencia.AttendanceSummaryDTO;
import org.example.centrosnetapi.models.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    @Query("""
SELECT new org.example.centrosnetapi.dtos.Asistencia.AttendanceSummaryDTO(
    a.id,
    a.nombre,
    SUM(CASE WHEN asi.estado = 'AUSENTE' THEN 1 ELSE 0 END),
    COUNT(asi),
    (100.0 * SUM(CASE WHEN asi.estado = 'PRESENTE' THEN 1 ELSE 0 END) / COUNT(asi))
)
FROM Asistencia asi
JOIN asi.sesion s
JOIN s.asignatura a
WHERE asi.alumno.id = :alumnoId
GROUP BY a.id, a.nombre
""")
    List<AttendanceSummaryDTO> getAttendanceSummaryByAlumno(Long alumnoId);

    List<Asistencia> findBySesionIdAndFecha(
            Long sesionId,
            LocalDate fecha
    );

    @Query("""
    SELECT new org.example.centrosnetapi.dtos.Asistencia.AttendanceDetailDTO(
        s.id,
        a.fecha,
        a.estado
    )
    FROM Asistencia a
    JOIN a.sesion s
    JOIN s.asignatura asig
    WHERE a.alumno.id = :alumnoId
      AND asig.id = :asignaturaId
    ORDER BY a.fecha DESC
""")
    List<AttendanceDetailDTO> findDetailByAlumnoAndAsignatura(
            @Param("alumnoId") Long alumnoId,
            @Param("asignaturaId") Long asignaturaId
    );
}