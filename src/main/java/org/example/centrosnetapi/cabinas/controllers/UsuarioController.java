package org.example.centrosnetapi.cabinas.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.cabinas.repositories.UsuarioRepository;
import org.example.centrosnetapi.models.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping("/buscar")
    public List<User> buscar(@RequestParam String query) {
        return usuarioRepository.buscarPorIdParcial(query);
    }
}