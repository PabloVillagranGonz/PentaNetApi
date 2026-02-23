package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByCenterId(Long centerId);

    @Query("""

            SELECT DISTINCT cs.subject
FROM ClassSession cs
WHERE cs.teacher.id = :teacherId
""")
    List<Subject> findSubjectsByTeacherId(@Param("teacherId") Long teacherId);
    }