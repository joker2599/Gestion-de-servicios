package com.techservice.dto;

import com.techservice.enums.EstadoDisponibilidad;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TecnicoRegistroRequest(
        @NotBlank(message = "El username es obligatorio")
        @Size(min = 3, max = 80, message = "El username debe tener entre 3 y 80 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username solo permite letras, números, punto, guion y guion bajo")
        String username,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Email inválido")
        @Size(max = 120, message = "El email no puede superar 120 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
        String password,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
        String apellido,

        @NotBlank(message = "La especialidad es obligatoria")
        @Size(max = 120, message = "La especialidad no puede superar 120 caracteres")
        String especialidad,

        EstadoDisponibilidad disponibilidad,

        @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
        String telefono
) {}
