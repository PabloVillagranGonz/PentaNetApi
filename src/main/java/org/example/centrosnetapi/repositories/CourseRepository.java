package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByCenterId(Long centerId);

}