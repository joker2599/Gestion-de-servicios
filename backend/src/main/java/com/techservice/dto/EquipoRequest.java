package com.techservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EquipoRequest(
        @NotNull(message = "El clienteId es obligatorio")
        @Positive(message = "El clienteId debe ser positivo")
        Long clienteId,

        @NotBlank(message = "El tipo es obligatorio")
        @Size(max = 80, message = "El tipo no puede superar 80 caracteres")
        String tipo,

        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 80, message = "La marca no puede superar 80 caracteres")
        String marca,

        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 80, message = "El modelo no puede superar 80 caracteres")
        String modelo,

        @NotBlank(message = "El número de serie es obligatorio")
        @Size(max = 100, message = "El número de serie no puede superar 100 caracteres")
        String numeroSerie,

        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String descripcion
) {}
