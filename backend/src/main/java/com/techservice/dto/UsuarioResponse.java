package com.techservice.dto;

import com.techservice.enums.EstadoUsuario;
import com.techservice.enums.RolUsuario;

import java.time.Instant;

public record UsuarioResponse(
        Long id,
        String username,
        String email,
        String nombre,
        String apellido,
        RolUsuario rol,
        EstadoUsuario estado,
        Instant creadoEn
) {}
