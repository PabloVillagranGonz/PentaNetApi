package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.CourseSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseSubjectRepository extends JpaRepository<CourseSubject, Long> {

    List<CourseSubject> findByCourseId(Long courseId);
    Optional<CourseSubject> findByCourseIdAndSubjectId(Long courseId, Long subjectId);
    List<CourseSubject> findBySubjectId(Long subjectId);
}