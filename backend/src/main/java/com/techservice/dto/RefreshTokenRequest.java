package com.techservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshTokenRequest(
        @NotBlank(message = "El refresh token es obligatorio")
        @Size(max = 2000, message = "Refresh token inválido")
        String refreshToken
) {}
