package org.example.centrosnetapi.dtos.Instrumento;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentResponseDTO {

    private Long id;
    private String name;
}