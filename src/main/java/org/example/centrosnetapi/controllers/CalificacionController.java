package org.example.centrosnetapi.controllers;

import jakarta.validation.Valid;
import org.example.centrosnetapi.dtos.Calificacion.*;
import org.example.centrosnetapi.models.SesionClase;
import org.example.centrosnetapi.repositories.SesionClaseRepository;
import org.example.centrosnetapi.services.CalificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluacion")
@CrossOrigin(origins = "*")
public class CalificacionController {

    @Autowired
    private CalificacionService calificacionService;

    @GetMapping("/grupo")
    public ResponseEntity<List<AlumnoEvaluacionDTO>> obtenerEvaluacionGrupo(
            @RequestParam Long asignaturaId,
            @RequestParam Long cursoId,
            @RequestParam(defaultValue = "false") boolean ocultarNoPublicadas) {

        List<AlumnoEvaluacionDTO> datos = calificacionService.obtenerEvaluacionGrupo(asignaturaId, cursoId, ocultarNoPublicadas);
        return ResponseEntity.ok(datos);
    }

    @PutMapping("/publicar")
    public ResponseEntity<String> publicarNotas(
            @RequestParam Long asignaturaId,
            @RequestParam Long cursoId,
            @RequestParam boolean publicadas) {
        try {
            calificacionService.publicarNotas(asignaturaId, cursoId, publicadas);
            return ResponseEntity.ok(publicadas ? "Notas publicadas" : "Notas ocultadas");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al modificar publicación: " + e.getMessage());
        }
    }

    @GetMapping("/alumno/{alumnoId}")
    public ResponseEntity<List<ResumenAsignaturaAlumnoDTO>> obtenerNotasAlumno(@PathVariable Long alumnoId) {
        return ResponseEntity.ok(calificacionService.obtenerNotasResumenAlumno(alumnoId));
    }

    @PostMapping("/nota")
    public ResponseEntity<String> guardarNota(@Valid @RequestBody GuardarNotaRequest request) {
        try {
            calificacionService.guardarNota(request);
            return ResponseEntity.ok("Nota guardada correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al guardar la nota: " + e.getMessage());
        }
    }

    // 👇 NUEVO ENDPOINT
    @PostMapping("/criterio")
    public ResponseEntity<String> crearCriterio(@Valid @RequestBody CrearCriterioRequest request) {
        try {
            calificacionService.crearCriterio(request.getAsignaturaId(), request.getCursoId(), request.getNombre(), request.getPeso());
            return ResponseEntity.ok("Criterio creado correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear el criterio: " + e.getMessage());
        }
    }

    @GetMapping("/profesor/{profesorId}/asignaturas")
    public ResponseEntity<?> obtenerAsignaturasProfesor(@PathVariable Long profesorId) {
        try {
            // Llama a tu servicio para buscar qué asignaturas da este profesor
            // (Ejemplo: calificacionService.obtenerAsignaturasPorProfesor(profesorId))
            return ResponseEntity.ok(calificacionService.obtenerAsignaturasPorProfesor(profesorId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al cargar las asignaturas: " + e.getMessage());
        }
    }
}