package com.techservice.dto;

public record ClienteResponse(
        Long id,
        Long usuarioId,
        String razonSocial,
        String documento,
        String telefono,
        String direccion,
        String ciudad
) {}
