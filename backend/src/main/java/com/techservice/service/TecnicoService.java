package com.techservice.service;

import com.techservice.dto.TecnicoRegistroRequest;
import com.techservice.dto.TecnicoRequest;
import com.techservice.dto.TecnicoResponse;
import com.techservice.dto.UsuarioRequest;
import com.techservice.entity.OrdenServicio;
import com.techservice.entity.Tecnico;
import com.techservice.entity.Usuario;
import com.techservice.enums.EstadoDisponibilidad;
import com.techservice.enums.EstadoOrden;
import com.techservice.enums.RolUsuario;
import com.techservice.exception.BusinessException;
import com.techservice.exception.ResourceNotFoundException;
import com.techservice.repository.OrdenServicioRepository;
import com.techservice.repository.TecnicoRepository;
import com.techservice.security.SecurityUtils;
import com.techservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TecnicoService {

    private static final Set<EstadoOrden> ESTADOS_CERRADOS = EnumSet.of(
            EstadoOrden.COMPLETADA, EstadoOrden.CERRADA, EstadoOrden.CANCELADA);

    private final TecnicoRepository tecnicoRepository;
    private final OrdenServicioRepository ordenServicioRepository;
    private final UsuarioService usuarioService;

    public List<TecnicoResponse> listar() {
        UserPrincipal current = SecurityUtils.requireCurrentUser();
        if (current.getRol() == RolUsuario.ADMIN) {
            return tecnicoRepository.findAll().stream().map(this::toResponse).toList();
        }
        if (current.getRol() == RolUsuario.TECNICO) {
            return tecnicoRepository.findByUsuarioId(current.getId()).stream()
                    .map(this::toResponse)
                    .toList();
        }
        // Cliente puede ver técnicos disponibles (solo lectura)
        return tecnicoRepository.findByDisponibilidad(EstadoDisponibilidad.DISPONIBLE).stream()
                .map(this::toResponse)
                .toList();
    }

    public TecnicoResponse obtener(Long id) {
        Tecnico tecnico = findEntity(id);
        UserPrincipal current = SecurityUtils.requireCurrentUser();
        if (current.getRol() == RolUsuario.TECNICO
                && !tecnico.getUsuario().getId().equals(current.getId())) {
            throw new AccessDeniedException("No autorizado a ver este técnico");
        }
        return toResponse(tecnico);
    }

    /** Usuarios TECNICO que aún no tienen fila en tecnicos (para el alta por FK). */
    public List<Long> usuarioIdsSinPerfil() {
        SecurityUtils.requireAdmin();
        Set<Long> vinculados = tecnicoRepository.findAll().stream()
                .map(t -> t.getUsuario().getId())
                .collect(Collectors.toSet());
        return usuarioService.listar().stream()
                .filter(u -> u.rol() == RolUsuario.TECNICO)
                .map(u -> u.id())
                .filter(id -> !vinculados.contains(id))
                .toList();
    }

    @Transactional
    public TecnicoResponse crear(TecnicoRequest request) {
        SecurityUtils.requireAdmin();
        Usuario usuario = usuarioService.findEntity(request.usuarioId());
        if (usuario.getRol() != RolUsuario.TECNICO) {
            throw new BusinessException("El usuario debe tener rol TECNICO");
        }
        tecnicoRepository.findByUsuarioId(usuario.getId()).ifPresent(t -> {
            throw new BusinessException("El usuario ya tiene perfil de técnico");
        });
        return toResponse(guardarPerfil(usuario, request.especialidad(), request.disponibilidad(), request.telefono()));
    }

    /**
     * Alta atómica: crea Usuario con rol TECNICO + perfil Tecnico.
     * Evita el caso típico de UI donde el único TECNICO del seed ya está vinculado.
     */
    @Transactional
    public TecnicoResponse registrar(TecnicoRegistroRequest request) {
        SecurityUtils.requireAdmin();
        var creado = usuarioService.crear(new UsuarioRequest(
                request.username(),
                request.email(),
                request.password(),
                request.nombre(),
                request.apellido(),
                RolUsuario.TECNICO
        ));
        Usuario usuario = usuarioService.findEntity(creado.id());
        return toResponse(guardarPerfil(usuario, request.especialidad(), request.disponibilidad(), request.telefono()));
    }

    private Tecnico guardarPerfil(Usuario usuario, String especialidad,
                                  EstadoDisponibilidad disponibilidad, String telefono) {
        Tecnico tecnico = Tecnico.builder()
                .usuario(usuario)
                .especialidad(especialidad)
                .disponibilidad(disponibilidad != null ? disponibilidad : EstadoDisponibilidad.DISPONIBLE)
                .telefono(telefono)
                .build();
        return tecnicoRepository.save(tecnico);
    }

    @Transactional
    public TecnicoResponse actualizar(Long id, TecnicoRequest request) {
        Tecnico tecnico = findEntity(id);
        UserPrincipal current = SecurityUtils.requireCurrentUser();
        boolean owner = tecnico.getUsuario().getId().equals(current.getId());
        if (current.getRol() != RolUsuario.ADMIN && !(current.getRol() == RolUsuario.TECNICO && owner)) {
            throw new AccessDeniedException("No autorizado a actualizar este técnico");
        }
        tecnico.setEspecialidad(request.especialidad());
        if (request.disponibilidad() != null) {
            tecnico.setDisponibilidad(request.disponibilidad());
        }
        tecnico.setTelefono(request.telefono());
        return toResponse(tecnico);
    }

    /**
     * Solo ADMIN. Reglas con órdenes:
     * - Si hay órdenes abiertas asignadas → 400 (reasignar/cerrar primero).
     * - Órdenes cerradas/completadas/canceladas → se desasignan (tecnico_id = null) y se borra el perfil.
     * - El Usuario asociado NO se elimina (queda libre para otro perfil).
     */
    @Transactional
    public void eliminar(Long id) {
        SecurityUtils.requireAdmin();
        if (!tecnicoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Técnico no encontrado: " + id);
        }
        List<OrdenServicio> asignadas = ordenServicioRepository.findByTecnicoId(id);
        long abiertas = asignadas.stream()
                .filter(o -> !ESTADOS_CERRADOS.contains(o.getEstado()))
                .count();
        if (abiertas > 0) {
            throw new BusinessException(
                    "No se puede eliminar: tiene " + abiertas
                            + " orden(es) abierta(s) asignada(s). Reasigna o cierra esas órdenes primero.");
        }
        for (OrdenServicio orden : asignadas) {
            orden.setTecnico(null);
        }
        tecnicoRepository.deleteById(id);
    }

    public Tecnico findEntity(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado: " + id));
    }

    public Tecnico findByUsuarioId(Long usuarioId) {
        return tecnicoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de técnico no encontrado para usuario " + usuarioId));
    }

    private TecnicoResponse toResponse(Tecnico t) {
        return new TecnicoResponse(
                t.getId(), t.getUsuario().getId(), t.getEspecialidad(),
                t.getDisponibilidad(), t.getTelefono()
        );
    }
}
