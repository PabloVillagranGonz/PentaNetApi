package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.CourseResponseDTO;
import org.example.centrosnetapi.dtos.SubjectResponseDTO;
import org.example.centrosnetapi.dtos.UserResponseDTO;
import org.example.centrosnetapi.models.Enrollment;
import org.example.centrosnetapi.models.TeachingAssignment;
import org.example.centrosnetapi.models.User;
import org.example.centrosnetapi.repositories.EnrollmentRepository;
import org.example.centrosnetapi.repositories.TeachingAssignmentRepository;
import org.springframework.stereotype.Service;
import org.example.centrosnetapi.models.Subject;
import org.example.centrosnetapi.models.Course;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final EnrollmentRepository enrollmentRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;

    // ============================
    // PROFESORES DEL ALUMNO
    // ============================
    public List<UserResponseDTO> getTeachersForStudent(Long studentId) {

        return enrollmentRepository.findByStudent_Id(studentId)
                .stream()
                .map(e -> e.getSubject().getId())
                .distinct()
                .flatMap(subjectId ->
                        teachingAssignmentRepository
                                .findBySubjectId(subjectId)
                                .stream()
                )
                .map(TeachingAssignment::getTeacher)
                .distinct()
                .map(this::toUserDTO)
                .toList();
    }

    // ============================
    // ASIGNATURAS DEL ALUMNO
    // ============================
    public List<Subject> getSubjectsForStudent(Long studentId) {
        return enrollmentRepository.findByStudent_Id(studentId)
                .stream()
                .map(Enrollment::getSubject)
                .distinct()
                .toList();
    }

    // ============================
    // CURSOS DEL ALUMNO
    // ============================
    public List<CourseResponseDTO> getCoursesForStudent(Long studentId) {

        return enrollmentRepository.findByStudent_Id(studentId)
                .stream()
                .map(Enrollment::getCourse)
                .filter(c -> c != null && c.getCenter() != null)
                .distinct()
                .map(this::toCourseDTO)
                .toList();
    }

    // ============================
    // MAPPERS
    // ============================
    private UserResponseDTO toUserDTO(User u) {
        return UserResponseDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellidos(u.getApellidos())
                .email(u.getEmail())
                .role(u.getRole())
                .centerId(u.getCenter() != null ? u.getCenter().getId() : null)
                .instrumentId(u.getInstrument() != null ? u.getInstrument().getId() : null)
                .build();
    }

    // ============================
// SUBJECT MAPPER
// ============================
    private SubjectResponseDTO toSubjectDTO(Subject s) {
        return SubjectResponseDTO.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .durationMinutes(s.getDurationMinutes())
                .centerId(
                        s.getCenter() != null ? s.getCenter().getId() : null
                )
                .build();
    }

    // ============================
// COURSE MAPPER
// ============================
    private CourseResponseDTO toCourseDTO(Course c) {
        return new CourseResponseDTO(
                c.getId(),
                c.getName(),
                c.getYear(),
                c.getNotes(),
                c.getCenter() != null ? c.getCenter().getId() : null
        );
    }

}