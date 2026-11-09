package com.techservice.service;

import com.techservice.dto.UsuarioRequest;
import com.techservice.dto.UsuarioResponse;
import com.techservice.entity.Usuario;
import com.techservice.enums.EstadoUsuario;
import com.techservice.exception.BusinessException;
import com.techservice.exception.ResourceNotFoundException;
import com.techservice.repository.UsuarioRepository;
import com.techservice.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> listar() {
        SecurityUtils.requireAdmin();
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UsuarioResponse obtener(Long id) {
        SecurityUtils.requireSelfOrAdmin(id);
        return toResponse(findEntity(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        SecurityUtils.requireAdmin();
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Ya existe un usuario con ese email");
        }
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new BusinessException("Ya existe un usuario con ese username");
        }
        Usuario usuario = Usuario.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .nombre(request.nombre())
                .apellido(request.apellido())
                .rol(request.rol())
                .estado(EstadoUsuario.ACTIVO)
                .build();
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        SecurityUtils.requireAdmin();
        Usuario usuario = findEntity(id);
        usuarioRepository.findByEmail(request.email())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> { throw new BusinessException("Email ya en uso"); });
        usuarioRepository.findByUsername(request.username())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> { throw new BusinessException("Username ya en uso"); });
        usuario.setUsername(request.username());
        usuario.setEmail(request.email());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setRol(request.rol());
        return toResponse(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        SecurityUtils.requireAdmin();
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    public Usuario findEntity(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getUsername(), u.getEmail(), u.getNombre(), u.getApellido(),
                u.getRol(), u.getEstado(), u.getCreadoEn()
        );
    }
}
