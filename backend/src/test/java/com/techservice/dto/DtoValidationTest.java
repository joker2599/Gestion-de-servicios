package com.techservice.dto;

import com.techservice.enums.PrioridadOrden;
import com.techservice.enums.RolUsuario;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    @Test
    void loginRequest_vacio_falla() {
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(new LoginRequest("", ""));
        assertThat(violations).isNotEmpty();
    }

    @Test
    void usuarioRequest_emailInvalido_falla() {
        UsuarioRequest req = new UsuarioRequest(
                "user1", "no-es-email", "secret1", "Nombre", null, RolUsuario.CLIENTE);
        Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void ordenRequest_equipoIdNegativo_falla() {
        OrdenServicioRequest req = new OrdenServicioRequest(-1L, "desc", PrioridadOrden.MEDIA, null);
        Set<ConstraintViolation<OrdenServicioRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("equipoId"));
    }

    @Test
    void detalleRequest_sinContenido_falla() {
        DetalleServicioRequest req = new DetalleServicioRequest(1L, "  ", null, "");
        Set<ConstraintViolation<DetalleServicioRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("diagnóstico")
                || v.getPropertyPath().toString().contains("contenidoPresente"));
    }

    @Test
    void asignarTecnico_null_falla() {
        Set<ConstraintViolation<AsignarTecnicoRequest>> violations =
                validator.validate(new AsignarTecnicoRequest(null));
        assertThat(violations).isNotEmpty();
    }

    @Test
    void loginRequest_valido_sinViolaciones() {
        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(new LoginRequest("admin", "Admin123!"));
        assertThat(violations).isEmpty();
    }
}
