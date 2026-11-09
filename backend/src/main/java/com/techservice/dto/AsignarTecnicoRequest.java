package com.techservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AsignarTecnicoRequest(
        @NotNull(message = "El tecnicoId es obligatorio")
        @Positive(message = "El tecnicoId debe ser positivo")
        Long tecnicoId
) {}
