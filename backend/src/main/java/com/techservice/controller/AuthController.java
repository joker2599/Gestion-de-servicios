package com.techservice.controller;

import com.techservice.dto.AuthResponse;
import com.techservice.dto.LoginRequest;
import com.techservice.dto.RefreshTokenRequest;
import com.techservice.dto.UsuarioResponse;
import com.techservice.security.SecurityUtils;
import com.techservice.security.UserPrincipal;
import com.techservice.service.AuthService;
import com.techservice.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticación JWT")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Login", description = "Devuelve accessToken y refreshToken")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Refresh token")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Usuario autenticado")
    public UsuarioResponse me() {
        UserPrincipal principal = SecurityUtils.requireCurrentUser();
        return usuarioService.obtener(principal.getId());
    }
}
