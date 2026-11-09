package com.techservice.dto;

import com.techservice.enums.EstadoOrden;
import jakarta.validation.constraints.NotNull;

public record ActualizarEstadoOrdenRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoOrden estado
) {}
