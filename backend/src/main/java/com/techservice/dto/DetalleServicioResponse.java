package com.techservice.dto;

import java.time.Instant;

public record DetalleServicioResponse(
        Long id,
        Long ordenServicioId,
        String diagnostico,
        String solucion,
        String observaciones,
        Instant registradoEn
) {}
