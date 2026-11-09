package com.techservice.support;

import com.techservice.entity.Usuario;
import com.techservice.enums.EstadoUsuario;
import com.techservice.enums.RolUsuario;
import com.techservice.security.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityTestUtils {

    private SecurityTestUtils() {}

    public static UserPrincipal authenticate(Long id, String username, RolUsuario rol) {
        Usuario usuario = Usuario.builder()
                .id(id)
                .username(username)
                .email(username + "@test.local")
                .passwordHash("hash")
                .nombre("Test")
                .rol(rol)
                .estado(EstadoUsuario.ACTIVO)
                .build();
        UserPrincipal principal = new UserPrincipal(usuario);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return principal;
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
