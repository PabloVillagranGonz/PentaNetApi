package org.example.centrosnetapi.services;


import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Instrumento.InstrumentResponseDTO;
import org.example.centrosnetapi.repositories.InstrumentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstrumentService {

    private final InstrumentoRepository instrumentRepository;

    public List<InstrumentResponseDTO> findAll() {
        return instrumentRepository.findAll()
                .stream()
                .map(i -> InstrumentResponseDTO.builder()
                        .id(i.getId())
                        .name(i.getNombre())
                        .build()
                )
                .toList();
    }
}
