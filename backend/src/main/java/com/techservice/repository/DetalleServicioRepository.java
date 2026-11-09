package com.techservice.repository;

import com.techservice.entity.DetalleServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleServicioRepository extends JpaRepository<DetalleServicio, Long> {
    List<DetalleServicio> findByOrdenServicioId(Long ordenServicioId);
}
