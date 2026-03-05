package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Instrumento.InstrumentResponseDTO;
import org.example.centrosnetapi.services.InstrumentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
@CrossOrigin
public class InstrumentsController {

    private final InstrumentService instrumentService;

    @GetMapping
    public List<InstrumentResponseDTO> getAll() {
        return instrumentService.findAll();
    }
}
