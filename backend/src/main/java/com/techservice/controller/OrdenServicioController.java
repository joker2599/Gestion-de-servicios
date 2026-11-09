package com.techservice.controller;

import com.techservice.dto.*;
import com.techservice.service.OrdenServicioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ordenes")
@RequiredArgsConstructor
@Tag(name = "Ordenes")
public class OrdenServicioController {

    private final OrdenServicioService ordenServicioService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<OrdenServicioResponse> listar() {
        return ordenServicioService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public OrdenServicioResponse obtener(@PathVariable Long id) {
        return ordenServicioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public OrdenServicioResponse crear(@Valid @RequestBody OrdenServicioRequest request) {
        return ordenServicioService.crear(request);
    }

    @PostMapping("/{id}/asignar-tecnico")
    @PreAuthorize("hasRole('ADMIN')")
    public OrdenServicioResponse asignarTecnico(
            @PathVariable Long id,
            @Valid @RequestBody AsignarTecnicoRequest request) {
        return ordenServicioService.asignarTecnico(id, request);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("isAuthenticated()")
    public OrdenServicioResponse actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoOrdenRequest request) {
        return ordenServicioService.actualizarEstado(id, request);
    }

    @GetMapping("/{id}/detalles")
    @PreAuthorize("isAuthenticated()")
    public List<DetalleServicioResponse> listarDetalles(@PathVariable Long id) {
        return ordenServicioService.listarDetalles(id);
    }

    @PostMapping("/detalles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public DetalleServicioResponse agregarDetalle(@Valid @RequestBody DetalleServicioRequest request) {
        return ordenServicioService.agregarDetalle(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        ordenServicioService.eliminar(id);
    }
}
