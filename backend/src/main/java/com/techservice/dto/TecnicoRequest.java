package com.techservice.dto;

import com.techservice.enums.EstadoDisponibilidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TecnicoRequest(
        @NotNull(message = "El usuarioId es obligatorio")
        @Positive(message = "El usuarioId debe ser positivo")
        Long usuarioId,

        @NotBlank(message = "La especialidad es obligatoria")
        @Size(max = 120, message = "La especialidad no puede superar 120 caracteres")
        String especialidad,

        EstadoDisponibilidad disponibilidad,

        @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
        String telefono
) {}
