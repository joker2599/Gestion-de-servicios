package com.techservice.controller;

import com.techservice.dto.TecnicoRegistroRequest;
import com.techservice.dto.TecnicoRequest;
import com.techservice.dto.TecnicoResponse;
import com.techservice.service.TecnicoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tecnicos")
@RequiredArgsConstructor
@Tag(name = "Tecnicos")
public class TecnicoController {

    private final TecnicoService tecnicoService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<TecnicoResponse> listar() {
        return tecnicoService.listar();
    }

    @GetMapping("/disponibles-usuario-ids")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Long> usuariosSinPerfil() {
        return tecnicoService.usuarioIdsSinPerfil();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public TecnicoResponse obtener(@PathVariable Long id) {
        return tecnicoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TecnicoResponse crear(@Valid @RequestBody TecnicoRequest request) {
        return tecnicoService.crear(request);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TecnicoResponse registrar(@Valid @RequestBody TecnicoRegistroRequest request) {
        return tecnicoService.registrar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public TecnicoResponse actualizar(@PathVariable Long id, @Valid @RequestBody TecnicoRequest request) {
        return tecnicoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        tecnicoService.eliminar(id);
    }
}
