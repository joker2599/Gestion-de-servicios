package com.techservice.dto;

import com.techservice.enums.EstadoDisponibilidad;

public record TecnicoResponse(
        Long id,
        Long usuarioId,
        String especialidad,
        EstadoDisponibilidad disponibilidad,
        String telefono
) {}
