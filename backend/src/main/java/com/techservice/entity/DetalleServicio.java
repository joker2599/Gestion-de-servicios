package com.techservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "detalles_servicio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_servicio_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_orden"))
    private OrdenServicio ordenServicio;

    @Column(length = 1000)
    private String diagnostico;

    @Column(length = 1000)
    private String solucion;

    @Column(length = 1000)
    private String observaciones;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant registradoEn = Instant.now();
}
