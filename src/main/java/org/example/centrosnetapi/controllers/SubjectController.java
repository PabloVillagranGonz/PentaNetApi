package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.SubjectRequestDTO;
import org.example.centrosnetapi.dtos.SubjectResponseDTO;
import org.example.centrosnetapi.services.SubjectService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@CrossOrigin
public class SubjectController {

    private final SubjectService subjectService;

    @PutMapping("/{id}")
    public SubjectResponseDTO update(
            @PathVariable Long id,
            @RequestBody SubjectRequestDTO dto
    ) {
        return subjectService.update(id, dto);
    }

    // CREATE
    @PostMapping
    public SubjectResponseDTO create(@RequestBody SubjectRequestDTO dto) {
        return subjectService.create(dto);
    }

    // READ ALL
    @GetMapping
    public List<SubjectResponseDTO> getAll() {
        return subjectService.findAll();
    }

    // READ BY CENTER
    @GetMapping("/center/{centerId}")
    public List<SubjectResponseDTO> getByCenter(@PathVariable Long centerId) {
        return subjectService.findByCenter(centerId);
    }

    // READ BY ID
    @GetMapping("/{id}")
    public SubjectResponseDTO getById(@PathVariable Long id) {
        return subjectService.findById(id);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        subjectService.delete(id);
    }

    @GetMapping("/mine")
    public List<SubjectResponseDTO> getMySubjects(Authentication authentication) {

        String email = authentication.getName();

        return subjectService.getSubjectsForTeacher(email);
    }
}