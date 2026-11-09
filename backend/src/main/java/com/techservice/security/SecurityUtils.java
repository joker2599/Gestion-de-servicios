package com.techservice.security;

import com.techservice.enums.RolUsuario;
import com.techservice.exception.BusinessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UserPrincipal requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("No autenticado");
        }
        return principal;
    }

    public static boolean hasRole(RolUsuario rol) {
        return requireCurrentUser().getRol() == rol;
    }

    public static boolean isAdmin() {
        return hasRole(RolUsuario.ADMIN);
    }

    public static void requireAdmin() {
        if (!isAdmin()) {
            throw new AccessDeniedException("Se requiere rol ADMIN");
        }
    }

    public static void requireSelfOrAdmin(Long usuarioId) {
        UserPrincipal current = requireCurrentUser();
        if (current.getRol() != RolUsuario.ADMIN && !current.getId().equals(usuarioId)) {
            throw new AccessDeniedException("No autorizado para este recurso");
        }
    }

    public static void deny(String message) {
        throw new AccessDeniedException(message);
    }

    public static void businessDeny(String message) {
        throw new BusinessException(message);
    }
}
