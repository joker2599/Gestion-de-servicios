package com.techservice.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DetalleServicioRequest(
        @NotNull(message = "El ordenServicioId es obligatorio")
        @Positive(message = "El ordenServicioId debe ser positivo")
        Long ordenServicioId,

        @Size(max = 1000, message = "El diagnóstico no puede superar 1000 caracteres")
        String diagnostico,

        @Size(max = 1000, message = "La solución no puede superar 1000 caracteres")
        String solucion,

        @Size(max = 1000, message = "Las observaciones no pueden superar 1000 caracteres")
        String observaciones
) {
    @AssertTrue(message = "Debe indicar al menos diagnóstico, solución u observaciones")
    public boolean isContenidoPresente() {
        return hasText(diagnostico) || hasText(solucion) || hasText(observaciones);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
