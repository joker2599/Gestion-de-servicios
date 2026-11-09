package com.techservice.dto;

public record EquipoResponse(
        Long id,
        Long clienteId,
        String tipo,
        String marca,
        String modelo,
        String numeroSerie,
        String descripcion
) {}
