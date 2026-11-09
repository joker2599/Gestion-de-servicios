package com.techservice.service;

import com.techservice.dto.ActualizarEstadoOrdenRequest;
import com.techservice.dto.AsignarTecnicoRequest;
import com.techservice.dto.OrdenServicioRequest;
import com.techservice.dto.OrdenServicioResponse;
import com.techservice.entity.*;
import com.techservice.enums.EstadoOrden;
import com.techservice.enums.EstadoUsuario;
import com.techservice.enums.PrioridadOrden;
import com.techservice.enums.RolUsuario;
import com.techservice.exception.BusinessException;
import com.techservice.exception.ResourceNotFoundException;
import com.techservice.repository.DetalleServicioRepository;
import com.techservice.repository.OrdenServicioRepository;
import com.techservice.support.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdenServicioServiceTest {

    @Mock
    private OrdenServicioRepository ordenRepository;
    @Mock
    private DetalleServicioRepository detalleRepository;
    @Mock
    private EquipoService equipoService;
    @Mock
    private TecnicoService tecnicoService;
    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private OrdenServicioService ordenServicioService;

    private Equipo equipo;
    private Tecnico tecnico;
    private OrdenServicio ordenPendiente;

    @BeforeEach
    void setUp() {
        Usuario clienteUser = Usuario.builder()
                .id(10L).username("cliente").email("c@t.local").passwordHash("x")
                .nombre("Ana").rol(RolUsuario.CLIENTE).estado(EstadoUsuario.ACTIVO).build();
        Cliente cliente = Cliente.builder().id(5L).usuario(clienteUser).razonSocial("Demo SAS").build();
        equipo = Equipo.builder()
                .id(20L).cliente(cliente).tipo("Laptop").marca("Dell").modelo("X")
                .numeroSerie("SN-1").build();
        Usuario tecnicoUser = Usuario.builder()
                .id(2L).username("tecnico").email("t@t.local").passwordHash("x")
                .nombre("Carlos").rol(RolUsuario.TECNICO).estado(EstadoUsuario.ACTIVO).build();
        tecnico = Tecnico.builder().id(7L).usuario(tecnicoUser).especialidad("Hardware").build();
        ordenPendiente = OrdenServicio.builder()
                .id(100L)
                .codigo("OS-TEST-0001")
                .descripcion("No enciende")
                .prioridad(PrioridadOrden.ALTA)
                .estado(EstadoOrden.PENDIENTE)
                .equipo(equipo)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clear();
    }

    @Test
    void crear_comoAdmin_persisteOrdenPendiente() {
        SecurityTestUtils.authenticate(1L, "admin", RolUsuario.ADMIN);
        when(equipoService.findEntity(20L)).thenReturn(equipo);
        doNothing().when(equipoService).assertCanView(equipo);
        when(ordenRepository.save(any(OrdenServicio.class))).thenAnswer(inv -> {
            OrdenServicio o = inv.getArgument(0);
            o.setId(101L);
            return o;
        });

        OrdenServicioResponse response = ordenServicioService.crear(
                new OrdenServicioRequest(20L, "Pantalla rota", PrioridadOrden.MEDIA, null));

        assertThat(response.estado()).isEqualTo(EstadoOrden.PENDIENTE);
        assertThat(response.equipoId()).isEqualTo(20L);
        assertThat(response.tecnicoId()).isNull();
        assertThat(response.codigo()).startsWith("OS-");
        verify(ordenRepository).save(any(OrdenServicio.class));
    }

    @Test
    void crear_comoAdmin_conTecnico_quedaAsignada() {
        SecurityTestUtils.authenticate(1L, "admin", RolUsuario.ADMIN);
        when(equipoService.findEntity(20L)).thenReturn(equipo);
        doNothing().when(equipoService).assertCanView(equipo);
        when(tecnicoService.findEntity(7L)).thenReturn(tecnico);
        when(ordenRepository.save(any(OrdenServicio.class))).thenAnswer(inv -> {
            OrdenServicio o = inv.getArgument(0);
            o.setId(102L);
            return o;
        });

        OrdenServicioResponse response = ordenServicioService.crear(
                new OrdenServicioRequest(20L, "Falla de disco", PrioridadOrden.ALTA, 7L));

        assertThat(response.estado()).isEqualTo(EstadoOrden.ASIGNADA);
        assertThat(response.tecnicoId()).isEqualTo(7L);
        assertThat(response.fechaAsignacion()).isNotNull();
    }

    @Test
    void crear_comoTecnico_denegado() {
        SecurityTestUtils.authenticate(2L, "tecnico", RolUsuario.TECNICO);
        when(equipoService.findEntity(20L)).thenReturn(equipo);
        doNothing().when(equipoService).assertCanView(equipo);

        assertThatThrownBy(() -> ordenServicioService.crear(
                new OrdenServicioRequest(20L, "Algo", PrioridadOrden.BAJA, null)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("técnico no crea");
        verify(ordenRepository, never()).save(any());
    }

    @Test
    void asignarTecnico_comoAdmin_actualizaOrden() {
        SecurityTestUtils.authenticate(1L, "admin", RolUsuario.ADMIN);
        when(ordenRepository.findById(100L)).thenReturn(Optional.of(ordenPendiente));
        when(tecnicoService.findEntity(7L)).thenReturn(tecnico);

        OrdenServicioResponse response = ordenServicioService.asignarTecnico(100L, new AsignarTecnicoRequest(7L));

        assertThat(response.estado()).isEqualTo(EstadoOrden.ASIGNADA);
        assertThat(response.tecnicoId()).isEqualTo(7L);
        assertThat(ordenPendiente.getFechaAsignacion()).isNotNull();
    }

    @Test
    void asignarTecnico_ordenCerrada_lanzaBusinessException() {
        SecurityTestUtils.authenticate(1L, "admin", RolUsuario.ADMIN);
        ordenPendiente.setEstado(EstadoOrden.CERRADA);
        when(ordenRepository.findById(100L)).thenReturn(Optional.of(ordenPendiente));

        assertThatThrownBy(() -> ordenServicioService.asignarTecnico(100L, new AsignarTecnicoRequest(7L)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CERRADA");
        verify(tecnicoService, never()).findEntity(any());
    }

    @Test
    void asignarTecnico_sinRolAdmin_denegado() {
        SecurityTestUtils.authenticate(2L, "tecnico", RolUsuario.TECNICO);

        assertThatThrownBy(() -> ordenServicioService.asignarTecnico(100L, new AsignarTecnicoRequest(7L)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void asignarTecnico_ordenInexistente_lanzaNotFound() {
        SecurityTestUtils.authenticate(1L, "admin", RolUsuario.ADMIN);
        when(ordenRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenServicioService.asignarTecnico(999L, new AsignarTecnicoRequest(7L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void actualizarEstado_tecnicoAsignado_puedeCompletar() {
        SecurityTestUtils.authenticate(2L, "tecnico", RolUsuario.TECNICO);
        ordenPendiente.setTecnico(tecnico);
        ordenPendiente.setEstado(EstadoOrden.ASIGNADA);
        when(ordenRepository.findById(100L)).thenReturn(Optional.of(ordenPendiente));
        when(tecnicoService.findByUsuarioId(2L)).thenReturn(tecnico);

        OrdenServicioResponse response = ordenServicioService.actualizarEstado(
                100L, new ActualizarEstadoOrdenRequest(EstadoOrden.COMPLETADA));

        assertThat(response.estado()).isEqualTo(EstadoOrden.COMPLETADA);
        assertThat(response.fechaCierre()).isNotNull();
    }

    @Test
    void actualizarEstado_tecnicoNoPuedeCancelar() {
        SecurityTestUtils.authenticate(2L, "tecnico", RolUsuario.TECNICO);
        ordenPendiente.setTecnico(tecnico);
        when(ordenRepository.findById(100L)).thenReturn(Optional.of(ordenPendiente));
        when(tecnicoService.findByUsuarioId(2L)).thenReturn(tecnico);

        assertThatThrownBy(() -> ordenServicioService.actualizarEstado(
                100L, new ActualizarEstadoOrdenRequest(EstadoOrden.CANCELADA)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("cancelar");
    }

    @Test
    void crear_capturaPrioridadPorDefectoMedia() {
        SecurityTestUtils.authenticate(1L, "admin", RolUsuario.ADMIN);
        when(equipoService.findEntity(20L)).thenReturn(equipo);
        doNothing().when(equipoService).assertCanView(equipo);
        ArgumentCaptor<OrdenServicio> captor = ArgumentCaptor.forClass(OrdenServicio.class);
        when(ordenRepository.save(captor.capture())).thenAnswer(inv -> {
            OrdenServicio o = inv.getArgument(0);
            o.setId(103L);
            return o;
        });

        ordenServicioService.crear(new OrdenServicioRequest(20L, "Sin prioridad", null, null));

        assertThat(captor.getValue().getPrioridad()).isEqualTo(PrioridadOrden.MEDIA);
    }
}
