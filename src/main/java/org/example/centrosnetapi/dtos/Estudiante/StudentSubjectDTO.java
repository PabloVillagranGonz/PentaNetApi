package org.example.centrosnetapi.dtos.Estudiante;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentSubjectDTO {

    private Long subjectId;
    private String subjectName;
    private String teacherName;
    private String espacioNombre;
}