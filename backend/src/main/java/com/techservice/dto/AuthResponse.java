package com.techservice.dto;

import com.techservice.enums.RolUsuario;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInMs,
        Long userId,
        String username,
        String email,
        RolUsuario rol
) {}
