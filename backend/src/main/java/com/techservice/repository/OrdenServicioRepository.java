package com.techservice.repository;

import com.techservice.entity.OrdenServicio;
import com.techservice.enums.EstadoOrden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdenServicioRepository extends JpaRepository<OrdenServicio, Long> {
    Optional<OrdenServicio> findByCodigo(String codigo);
    List<OrdenServicio> findByEstado(EstadoOrden estado);
    List<OrdenServicio> findByTecnicoId(Long tecnicoId);
    List<OrdenServicio> findByEquipoClienteId(Long clienteId);
}
