package com.techservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotNull(message = "El usuarioId es obligatorio")
        @Positive(message = "El usuarioId debe ser positivo")
        Long usuarioId,

        @NotBlank(message = "La razón social es obligatoria")
        @Size(max = 150, message = "La razón social no puede superar 150 caracteres")
        String razonSocial,

        @Size(max = 30, message = "El documento no puede superar 30 caracteres")
        String documento,

        @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
        String telefono,

        @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
        String direccion,

        @Size(max = 100, message = "La ciudad no puede superar 100 caracteres")
        String ciudad
) {}
