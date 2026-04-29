package org.example.centrosnetapi.controllers;

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

    @Autowired
    private SesionClaseRepository sesionClaseRepository;

    @GetMapping("/grupo")
    public ResponseEntity<List<AlumnoEvaluacionDTO>> obtenerEvaluacionGrupo(
            @RequestParam Long asignaturaId,
            @RequestParam Long cursoId) {

        List<AlumnoEvaluacionDTO> datos = calificacionService.obtenerEvaluacionGrupo(asignaturaId, cursoId);
        return ResponseEntity.ok(datos);
    }

    @GetMapping("/alumno/{alumnoId}")
    public ResponseEntity<List<ResumenAsignaturaAlumnoDTO>> obtenerNotasAlumno(@PathVariable Long alumnoId) {
        return ResponseEntity.ok(calificacionService.obtenerNotasResumenAlumno(alumnoId));
    }

    @PostMapping("/nota")
    public ResponseEntity<String> guardarNota(@RequestBody GuardarNotaRequest request) {
        try {
            calificacionService.guardarNota(request);
            return ResponseEntity.ok("Nota guardada correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al guardar la nota: " + e.getMessage());
        }
    }

    // 👇 NUEVO ENDPOINT
    @PostMapping("/criterio")
    public ResponseEntity<String> crearCriterio(@RequestBody CrearCriterioRequest request) {
        try {
            calificacionService.crearCriterio(request.getAsignaturaId(), request.getCursoId(), request.getNombre(), request.getPeso());
            return ResponseEntity.ok("Criterio creado correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear el criterio: " + e.getMessage());
        }
    }
}