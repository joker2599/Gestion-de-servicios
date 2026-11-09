package com.techservice.dto;

import com.techservice.enums.EstadoOrden;
import com.techservice.enums.PrioridadOrden;

import java.time.Instant;

public record OrdenServicioResponse(
        Long id,
        String codigo,
        String descripcion,
        PrioridadOrden prioridad,
        EstadoOrden estado,
        Long equipoId,
        Long tecnicoId,
        Instant fechaCreacion,
        Instant fechaAsignacion,
        Instant fechaCierre
) {}
