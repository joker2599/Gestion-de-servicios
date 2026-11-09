package com.techservice.service;

import com.techservice.dto.*;
import com.techservice.entity.DetalleServicio;
import com.techservice.entity.Equipo;
import com.techservice.entity.OrdenServicio;
import com.techservice.entity.Tecnico;
import com.techservice.enums.EstadoOrden;
import com.techservice.enums.PrioridadOrden;
import com.techservice.enums.RolUsuario;
import com.techservice.exception.BusinessException;
import com.techservice.exception.ResourceNotFoundException;
import com.techservice.repository.DetalleServicioRepository;
import com.techservice.repository.OrdenServicioRepository;
import com.techservice.security.SecurityUtils;
import com.techservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrdenServicioService {

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    private final OrdenServicioRepository ordenRepository;
    private final DetalleServicioRepository detalleRepository;
    private final EquipoService equipoService;
    private final TecnicoService tecnicoService;
    private final ClienteService clienteService;

    public List<OrdenServicioResponse> listar() {
        UserPrincipal current = SecurityUtils.requireCurrentUser();
        return switch (current.getRol()) {
            case ADMIN -> ordenRepository.findAll().stream().map(this::toResponse).toList();
            case TECNICO -> {
                Tecnico tecnico = tecnicoService.findByUsuarioId(current.getId());
                yield ordenRepository.findByTecnicoId(tecnico.getId()).stream().map(this::toResponse).toList();
            }
            case CLIENTE -> {
                var cliente = clienteService.findByUsuarioId(current.getId());
                yield ordenRepository.findByEquipoClienteId(cliente.getId()).stream().map(this::toResponse).toList();
            }
        };
    }

    public OrdenServicioResponse obtener(Long id) {
        OrdenServicio orden = findEntity(id);
        assertCanView(orden);
        return toResponse(orden);
    }

    @Transactional
    public OrdenServicioResponse crear(OrdenServicioRequest request) {
        UserPrincipal current = SecurityUtils.requireCurrentUser();
        Equipo equipo = equipoService.findEntity(request.equipoId());
        equipoService.assertCanView(equipo);

        if (current.getRol() == RolUsuario.TECNICO) {
            throw new AccessDeniedException("El técnico no crea órdenes; solo las atiende");
        }
        if (current.getRol() == RolUsuario.CLIENTE
                && !equipo.getCliente().getUsuario().getId().equals(current.getId())) {
            throw new AccessDeniedException("Solo puede crear órdenes sobre sus equipos");
        }

        OrdenServicio orden = OrdenServicio.builder()
                .codigo(generarCodigo())
                .descripcion(request.descripcion())
                .prioridad(request.prioridad() != null ? request.prioridad() : PrioridadOrden.MEDIA)
                .estado(EstadoOrden.PENDIENTE)
                .equipo(equipo)
                .build();

        if (request.tecnicoId() != null) {
            if (current.getRol() != RolUsuario.ADMIN) {
                throw new AccessDeniedException("Solo ADMIN puede asignar técnico al crear la orden");
            }
            Tecnico tecnico = tecnicoService.findEntity(request.tecnicoId());
            orden.setTecnico(tecnico);
            orden.setEstado(EstadoOrden.ASIGNADA);
            orden.setFechaAsignacion(Instant.now());
        }
        return toResponse(ordenRepository.save(orden));
    }

    @Transactional
    public OrdenServicioResponse asignarTecnico(Long ordenId, AsignarTecnicoRequest request) {
        SecurityUtils.requireAdmin();
        OrdenServicio orden = findEntity(ordenId);
        if (orden.getEstado() == EstadoOrden.CANCELADA || orden.getEstado() == EstadoOrden.CERRADA) {
            throw new BusinessException("No se puede asignar técnico a una orden " + orden.getEstado());
        }
        Tecnico tecnico = tecnicoService.findEntity(request.tecnicoId());
        orden.setTecnico(tecnico);
        orden.setEstado(EstadoOrden.ASIGNADA);
        orden.setFechaAsignacion(Instant.now());
        return toResponse(orden);
    }

    @Transactional
    public OrdenServicioResponse actualizarEstado(Long ordenId, ActualizarEstadoOrdenRequest request) {
        OrdenServicio orden = findEntity(ordenId);
        UserPrincipal current = SecurityUtils.requireCurrentUser();

        if (current.getRol() == RolUsuario.ADMIN) {
            // ok
        } else if (current.getRol() == RolUsuario.TECNICO) {
            assertAssignedToCurrentTecnico(orden, current);
            // técnico no cancela/cierra administrativamente salvo completar flujo operativo
            if (request.estado() == EstadoOrden.CANCELADA) {
                throw new AccessDeniedException("El técnico no puede cancelar órdenes");
            }
        } else if (current.getRol() == RolUsuario.CLIENTE) {
            assertOwnedByCurrentCliente(orden, current);
            if (request.estado() != EstadoOrden.CANCELADA) {
                throw new AccessDeniedException("El cliente solo puede cancelar su orden");
            }
            if (orden.getEstado() != EstadoOrden.PENDIENTE && orden.getEstado() != EstadoOrden.ASIGNADA) {
                throw new BusinessException("Solo se pueden cancelar órdenes pendientes o asignadas");
            }
        } else {
            throw new AccessDeniedException("No autorizado");
        }

        orden.setEstado(request.estado());
        if (request.estado() == EstadoOrden.COMPLETADA
                || request.estado() == EstadoOrden.CERRADA
                || request.estado() == EstadoOrden.CANCELADA) {
            orden.setFechaCierre(Instant.now());
        }
        return toResponse(orden);
    }

    @Transactional
    public DetalleServicioResponse agregarDetalle(DetalleServicioRequest request) {
        OrdenServicio orden = findEntity(request.ordenServicioId());
        UserPrincipal current = SecurityUtils.requireCurrentUser();
        if (current.getRol() == RolUsuario.ADMIN) {
            // ok
        } else if (current.getRol() == RolUsuario.TECNICO) {
            assertAssignedToCurrentTecnico(orden, current);
        } else {
            throw new AccessDeniedException("Solo ADMIN o TECNICO asignado pueden registrar detalles");
        }

        DetalleServicio detalle = DetalleServicio.builder()
                .ordenServicio(orden)
                .diagnostico(request.diagnostico())
                .solucion(request.solucion())
                .observaciones(request.observaciones())
                .build();
        if (orden.getEstado() == EstadoOrden.ASIGNADA || orden.getEstado() == EstadoOrden.PENDIENTE) {
            orden.setEstado(EstadoOrden.EN_PROCESO);
        }
        return toDetalleResponse(detalleRepository.save(detalle));
    }

    public List<DetalleServicioResponse> listarDetalles(Long ordenId) {
        OrdenServicio orden = findEntity(ordenId);
        assertCanView(orden);
        return detalleRepository.findByOrdenServicioId(ordenId).stream()
                .map(this::toDetalleResponse)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        SecurityUtils.requireAdmin();
        if (!ordenRepository.existsById(id)) {
            throw new ResourceNotFoundException("Orden no encontrada: " + id);
        }
        ordenRepository.deleteById(id);
    }

    public OrdenServicio findEntity(Long id) {
        return ordenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + id));
    }

    private void assertCanView(OrdenServicio orden) {
        UserPrincipal current = SecurityUtils.requireCurrentUser();
        switch (current.getRol()) {
            case ADMIN -> { }
            case TECNICO -> assertAssignedToCurrentTecnico(orden, current);
            case CLIENTE -> assertOwnedByCurrentCliente(orden, current);
        }
    }

    private void assertAssignedToCurrentTecnico(OrdenServicio orden, UserPrincipal current) {
        Tecnico tecnico = tecnicoService.findByUsuarioId(current.getId());
        if (orden.getTecnico() == null || !orden.getTecnico().getId().equals(tecnico.getId())) {
            throw new AccessDeniedException("La orden no está asignada a este técnico");
        }
    }

    private void assertOwnedByCurrentCliente(OrdenServicio orden, UserPrincipal current) {
        if (!orden.getEquipo().getCliente().getUsuario().getId().equals(current.getId())) {
            throw new AccessDeniedException("La orden no pertenece a este cliente");
        }
    }

    private String generarCodigo() {
        String fecha = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "OS-" + fecha + "-" + String.format("%04d", SEQ.getAndIncrement());
    }

    private OrdenServicioResponse toResponse(OrdenServicio o) {
        return new OrdenServicioResponse(
                o.getId(), o.getCodigo(), o.getDescripcion(), o.getPrioridad(), o.getEstado(),
                o.getEquipo().getId(),
                o.getTecnico() != null ? o.getTecnico().getId() : null,
                o.getFechaCreacion(), o.getFechaAsignacion(), o.getFechaCierre()
        );
    }

    private DetalleServicioResponse toDetalleResponse(DetalleServicio d) {
        return new DetalleServicioResponse(
                d.getId(), d.getOrdenServicio().getId(), d.getDiagnostico(),
                d.getSolucion(), d.getObservaciones(), d.getRegistradoEn()
        );
    }
}
