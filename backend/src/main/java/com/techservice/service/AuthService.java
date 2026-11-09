package com.techservice.service;

import com.techservice.dto.AuthResponse;
import com.techservice.dto.LoginRequest;
import com.techservice.dto.RefreshTokenRequest;
import com.techservice.security.CustomUserDetailsService;
import com.techservice.security.JwtProperties;
import com.techservice.security.JwtService;
import com.techservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService userDetailsService;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return buildResponse(principal);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();
        if (!jwtService.isRefreshToken(token)) {
            throw new org.springframework.security.authentication.BadCredentialsException("Refresh token inválido");
        }
        String username = jwtService.extractUsername(token);
        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(username);
        if (!jwtService.isTokenValid(token, principal)) {
            throw new org.springframework.security.authentication.BadCredentialsException("Refresh token expirado");
        }
        return buildResponse(principal);
    }

    private AuthResponse buildResponse(UserPrincipal principal) {
        return new AuthResponse(
                jwtService.generateAccessToken(principal),
                jwtService.generateRefreshToken(principal),
                "Bearer",
                jwtProperties.getAccessTokenExpirationMs(),
                principal.getId(),
                principal.getUsername(),
                principal.getEmail(),
                principal.getRol()
        );
    }
}
