package com.techservice.service;

import com.techservice.dto.EquipoRequest;
import com.techservice.dto.EquipoResponse;
import com.techservice.entity.Cliente;
import com.techservice.entity.Equipo;
import com.techservice.enums.RolUsuario;
import com.techservice.exception.BusinessException;
import com.techservice.exception.ResourceNotFoundException;
import com.techservice.repository.EquipoRepository;
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
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final ClienteService clienteService;

    public List<EquipoResponse> listar() {
        UserPrincipal current = SecurityUtils.requireCurrentUser();
        if (current.getRol() == RolUsuario.ADMIN || current.getRol() == RolUsuario.TECNICO) {
            return equipoRepository.findAll().stream().map(this::toResponse).toList();
        }
        Cliente propio = clienteService.findByUsuarioId(current.getId());
        return equipoRepository.findByClienteId(propio.getId()).stream().map(this::toResponse).toList();
    }

    public List<EquipoResponse> listarPorCliente(Long clienteId) {
        Cliente cliente = clienteService.findEntity(clienteId);
        clienteService.assertCanView(cliente);
        UserPrincipal current = SecurityUtils.requireCurrentUser();
        if (current.getRol() == RolUsuario.CLIENTE
                && !cliente.getUsuario().getId().equals(current.getId())) {
            throw new AccessDeniedException("Solo puede listar sus propios equipos");
        }
        return equipoRepository.findByClienteId(clienteId).stream().map(this::toResponse).toList();
    }

    public EquipoResponse obtener(Long id) {
        Equipo equipo = findEntity(id);
        assertCanView(equipo);
        return toResponse(equipo);
    }

    @Transactional
    public EquipoResponse crear(EquipoRequest request) {
        UserPrincipal current = SecurityUtils.requireCurrentUser();
        Long clienteId = request.clienteId();
        if (current.getRol() == RolUsuario.CLIENTE) {
            Cliente propio = clienteService.findByUsuarioId(current.getId());
            clienteId = propio.getId();
        } else if (current.getRol() != RolUsuario.ADMIN) {
            throw new AccessDeniedException("Solo ADMIN o CLIENTE pueden registrar equipos");
        }
        if (equipoRepository.existsByNumeroSerie(request.numeroSerie())) {
            throw new BusinessException("Ya existe un equipo con ese número de serie");
        }
        Cliente cliente = clienteService.findEntity(clienteId);
        Equipo equipo = Equipo.builder()
                .cliente(cliente)
                .tipo(request.tipo())
                .marca(request.marca())
                .modelo(request.modelo())
                .numeroSerie(request.numeroSerie())
                .descripcion(request.descripcion())
                .build();
        return toResponse(equipoRepository.save(equipo));
    }

    @Transactional
    public EquipoResponse actualizar(Long id, EquipoRequest request) {
        Equipo equipo = findEntity(id);
        UserPrincipal current = SecurityUtils.requireCurrentUser();
        if (current.getRol() == RolUsuario.ADMIN) {
            // ok
        } else if (current.getRol() == RolUsuario.CLIENTE
                && equipo.getCliente().getUsuario().getId().equals(current.getId())) {
            // no cambiar de dueño
            if (!request.clienteId().equals(equipo.getCliente().getId())) {
                throw new AccessDeniedException("No puede reasignar el equipo a otro cliente");
            }
        } else {
            throw new AccessDeniedException("No autorizado a actualizar este equipo");
        }
        equipoRepository.findByNumeroSerie(request.numeroSerie())
                .filter(e -> !e.getId().equals(id))
                .ifPresent(e -> { throw new BusinessException("Número de serie ya en uso"); });
        if (current.getRol() == RolUsuario.ADMIN) {
            equipo.setCliente(clienteService.findEntity(request.clienteId()));
        }
        equipo.setTipo(request.tipo());
        equipo.setMarca(request.marca());
        equipo.setModelo(request.modelo());
        equipo.setNumeroSerie(request.numeroSerie());
        equipo.setDescripcion(request.descripcion());
        return toResponse(equipo);
    }

    @Transactional
    public void eliminar(Long id) {
        Equipo equipo = findEntity(id);
        UserPrincipal current = SecurityUtils.requireCurrentUser();
        if (current.getRol() == RolUsuario.ADMIN) {
            equipoRepository.deleteById(id);
            return;
        }
        if (current.getRol() == RolUsuario.CLIENTE
                && equipo.getCliente().getUsuario().getId().equals(current.getId())) {
            equipoRepository.deleteById(id);
            return;
        }
        throw new AccessDeniedException("No autorizado a eliminar este equipo");
    }

    public Equipo findEntity(Long id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado: " + id));
    }

    public void assertCanView(Equipo equipo) {
        UserPrincipal current = SecurityUtils.requireCurrentUser();
        if (current.getRol() == RolUsuario.ADMIN || current.getRol() == RolUsuario.TECNICO) {
            return;
        }
        if (current.getRol() == RolUsuario.CLIENTE
                && equipo.getCliente().getUsuario().getId().equals(current.getId())) {
            return;
        }
        throw new AccessDeniedException("No autorizado a ver este equipo");
    }

    private EquipoResponse toResponse(Equipo e) {
        return new EquipoResponse(
                e.getId(), e.getCliente().getId(), e.getTipo(), e.getMarca(),
                e.getModelo(), e.getNumeroSerie(), e.getDescripcion()
        );
    }
}
