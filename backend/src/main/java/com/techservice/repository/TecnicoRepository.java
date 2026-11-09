package com.techservice.repository;

import com.techservice.entity.Tecnico;
import com.techservice.enums.EstadoDisponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
    Optional<Tecnico> findByUsuarioId(Long usuarioId);
    List<Tecnico> findByDisponibilidad(EstadoDisponibilidad disponibilidad);
}
