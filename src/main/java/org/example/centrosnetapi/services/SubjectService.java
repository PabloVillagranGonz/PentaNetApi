package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.SubjectRequestDTO;
import org.example.centrosnetapi.dtos.SubjectResponseDTO;
import org.example.centrosnetapi.models.Center;
import org.example.centrosnetapi.models.Role;
import org.example.centrosnetapi.models.Subject;
import org.example.centrosnetapi.models.User;
import org.example.centrosnetapi.repositories.CenterRepository;
import org.example.centrosnetapi.repositories.SubjectRepository;
import org.example.centrosnetapi.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final CenterRepository centerRepository;
    private final UserRepository userRepository;

    // ================= UPDATE =================
    @PreAuthorize("hasRole('ADMIN')")
    public SubjectResponseDTO update(Long id, SubjectRequestDTO dto) {

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "SUBJECT_NOT_FOUND"
                        )
                );

        subject.setName(dto.getName());
        subject.setDescription(dto.getDescription());
        subject.setDurationMinutes(dto.getDurationMinutes());

        if (dto.getCenterId() != null) {
            Center center = centerRepository.findById(dto.getCenterId())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "CENTER_NOT_FOUND"
                            )
                    );
            subject.setCenter(center);
        }

        return toDTO(subjectRepository.save(subject));
    }

    // ================= CREATE =================
    @PreAuthorize("hasRole('ADMIN')")
    public SubjectResponseDTO create(SubjectRequestDTO dto) {

        Center center = centerRepository.findById(dto.getCenterId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "CENTER_NOT_FOUND"
                        )
                );

        Subject subject = new Subject();
        subject.setName(dto.getName());
        subject.setDescription(dto.getDescription());
        subject.setDurationMinutes(dto.getDurationMinutes());
        subject.setCenter(center);

        return toDTO(subjectRepository.save(subject));
    }

    // ================= READ =================
    @PreAuthorize("isAuthenticated()")
    public List<SubjectResponseDTO> findAll() {
        return subjectRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    public List<SubjectResponseDTO> findByCenter(Long centerId) {
        return subjectRepository.findByCenterId(centerId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    public SubjectResponseDTO findById(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "SUBJECT_NOT_FOUND"
                        )
                );

        return toDTO(subject);
    }

    // ================= DELETE =================
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "SUBJECT_NOT_FOUND"
            );
        }
        subjectRepository.deleteById(id);
    }

    public List<SubjectResponseDTO> getSubjectsForTeacher(String email) {

        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (teacher.getRole() != Role.TEACHER) {
            throw new RuntimeException("NOT_A_TEACHER");
        }

        return subjectRepository.findSubjectsByTeacherId(teacher.getId())
                .stream()
                .map(subject -> SubjectResponseDTO.builder()
                        .id(subject.getId())
                        .name(subject.getName())
                        .description(subject.getDescription())
                        .durationMinutes(subject.getDurationMinutes())
                        .centerId(subject.getCenter().getId())
                        .build())
                .toList();
    }

    // ================= DTO =================
    private SubjectResponseDTO toDTO(Subject subject) {
        return SubjectResponseDTO.builder()
                .id(subject.getId())
                .name(subject.getName())
                .description(subject.getDescription())
                .durationMinutes(subject.getDurationMinutes())
                .centerId(
                        subject.getCenter() != null
                                ? subject.getCenter().getId()
                                : null
                )
                .build();
    }
}