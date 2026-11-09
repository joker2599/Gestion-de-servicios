package com.techservice.dto;

import com.techservice.enums.PrioridadOrden;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OrdenServicioRequest(
        @NotNull(message = "El equipoId es obligatorio")
        @Positive(message = "El equipoId debe ser positivo")
        Long equipoId,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String descripcion,

        PrioridadOrden prioridad,

        @Positive(message = "El tecnicoId debe ser positivo")
        Long tecnicoId
) {}
