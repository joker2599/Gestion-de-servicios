package com.techservice.service;

import com.techservice.dto.ClienteRequest;
import com.techservice.dto.ClienteResponse;
import com.techservice.entity.Cliente;
import com.techservice.entity.Usuario;
import com.techservice.enums.RolUsuario;
import com.techservice.exception.BusinessException;
import com.techservice.exception.ResourceNotFoundException;
import com.techservice.repository.ClienteRepository;
import com.techservice.security.SecurityUtils;
import com.techservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioService usuarioService;

    public List<ClienteResponse> listar() {
        UserPrincipal current = SecurityUtils.requireCurrentUser();
        if (current.getRol() == RolUsuario.ADMIN) {
            return clienteRepository.findAll().stream().map(this::toResponse).toList();
        }
        if (current.getRol() == RolUsuario.CLIENTE) {
            return clienteRepository.findByUsuarioId(current.getId()).stream()
                    .map(this::toResponse)
                    .toList();
        }
        throw new AccessDeniedException("Técnicos no listan clientes");
    }

    public ClienteResponse obtener(Long id) {
        Cliente cliente = findEntity(id);
        assertCanView(cliente);
        return toResponse(cliente);
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        SecurityUtils.requireAdmin();
        Usuario usuario = usuarioService.findEntity(request.usuarioId());
        if (usuario.getRol() != RolUsuario.CLIENTE) {
            throw new BusinessException("El usuario debe tener rol CLIENTE");
        }
        clienteRepository.findByUsuarioId(usuario.getId()).ifPresent(c -> {
            throw new BusinessException("El usuario ya tiene perfil de cliente");
        });
        Cliente cliente = Cliente.builder()
                .usuario(usuario)
                .razonSocial(request.razonSocial())
                .documento(request.documento())
                .telefono(request.telefono())
                .direccion(request.direccion())
                .ciudad(request.ciudad())
                .build();
        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = findEntity(id);
        UserPrincipal current = SecurityUtils.requireCurrentUser();
        if (current.getRol() == RolUsuario.ADMIN) {
            // ok
        } else if (current.getRol() == RolUsuario.CLIENTE
                && cliente.getUsuario().getId().equals(current.getId())) {
            // cliente puede actualizar su perfil (sin cambiar usuarioId)
        } else {
            throw new AccessDeniedException("No autorizado a actualizar este cliente");
        }
        cliente.setRazonSocial(request.razonSocial());
        cliente.setDocumento(request.documento());
        cliente.setTelefono(request.telefono());
        cliente.setDireccion(request.direccion());
        cliente.setCiudad(request.ciudad());
        return toResponse(cliente);
    }

    @Transactional
    public void eliminar(Long id) {
        SecurityUtils.requireAdmin();
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente no encontrado: " + id);
        }
        clienteRepository.deleteById(id);
    }

    public Cliente findEntity(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
    }

    public Cliente findByUsuarioId(Long usuarioId) {
        return clienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de cliente no encontrado para usuario " + usuarioId));
    }

    public void assertCanView(Cliente cliente) {
        UserPrincipal current = SecurityUtils.requireCurrentUser();
        if (current.getRol() == RolUsuario.ADMIN || current.getRol() == RolUsuario.TECNICO) {
            return;
        }
        if (current.getRol() == RolUsuario.CLIENTE
                && cliente.getUsuario().getId().equals(current.getId())) {
            return;
        }
        throw new AccessDeniedException("No autorizado a ver este cliente");
    }

    private ClienteResponse toResponse(Cliente c) {
        return new ClienteResponse(
                c.getId(), c.getUsuario().getId(), c.getRazonSocial(),
                c.getDocumento(), c.getTelefono(), c.getDireccion(), c.getCiudad()
        );
    }
}
