package org.example.centrosnetapi.dtos;

import lombok.Data;

@Data
public class SendGroupCorreoRequestDTO {

    private String asunto;
    private String cuerpo;

    private Long subjectId;
}