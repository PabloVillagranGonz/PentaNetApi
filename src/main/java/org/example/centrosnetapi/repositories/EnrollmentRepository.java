package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.Enrollment;
import org.example.centrosnetapi.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndCourseIdAndSubjectId(
            Long studentId,
            Long courseId,
            Long subjectId
    );

    List<Enrollment> findByStudent_Id(Long studentId);

    @Query("""
    SELECT e.student
    FROM Enrollment e
    WHERE e.subject.id = :subjectId
""")
    List<User> findStudentsBySubjectId(@Param("subjectId") Long subjectId);
}