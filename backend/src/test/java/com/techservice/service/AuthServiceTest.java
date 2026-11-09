package com.techservice.service;

import com.techservice.dto.AuthResponse;
import com.techservice.dto.LoginRequest;
import com.techservice.dto.RefreshTokenRequest;
import com.techservice.entity.Usuario;
import com.techservice.enums.EstadoUsuario;
import com.techservice.enums.RolUsuario;
import com.techservice.security.CustomUserDetailsService;
import com.techservice.security.JwtProperties;
import com.techservice.security.JwtService;
import com.techservice.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .username("admin")
                .email("admin@techservice.local")
                .passwordHash("bcrypt")
                .nombre("Admin")
                .rol(RolUsuario.ADMIN)
                .estado(EstadoUsuario.ACTIVO)
                .build();
        principal = new UserPrincipal(usuario);
    }

    @Test
    void login_exitoso_devuelveTokens() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateAccessToken(principal)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(principal)).thenReturn("refresh-token");
        when(jwtProperties.getAccessTokenExpirationMs()).thenReturn(3_600_000L);

        AuthResponse response = authService.login(new LoginRequest("admin", "Admin123!"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.rol()).isEqualTo(RolUsuario.ADMIN);
        assertThat(response.expiresInMs()).isEqualTo(3_600_000L);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_credencialesInvalidas_lanzaBadCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refresh_conTokenValido_renuevaSesion() {
        when(jwtService.isRefreshToken("refresh-ok")).thenReturn(true);
        when(jwtService.extractUsername("refresh-ok")).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(principal);
        when(jwtService.isTokenValid("refresh-ok", principal)).thenReturn(true);
        when(jwtService.generateAccessToken(principal)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(principal)).thenReturn("new-refresh");
        when(jwtProperties.getAccessTokenExpirationMs()).thenReturn(3_600_000L);

        AuthResponse response = authService.refresh(new RefreshTokenRequest("refresh-ok"));

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
        assertThat(response.userId()).isEqualTo(1L);
    }

    @Test
    void refresh_conAccessToken_lanzaBadCredentials() {
        when(jwtService.isRefreshToken("access-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("access-token")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Refresh token inválido");
    }

    @Test
    void refresh_tokenExpirado_lanzaBadCredentials() {
        when(jwtService.isRefreshToken("expired")).thenReturn(true);
        when(jwtService.extractUsername("expired")).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(principal);
        when(jwtService.isTokenValid("expired", principal)).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("expired")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("expirado");
    }
}
