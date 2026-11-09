package com.techservice.entity;

import com.techservice.enums.EstadoOrden;
import com.techservice.enums.PrioridadOrden;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes_servicio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String codigo;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PrioridadOrden prioridad = PrioridadOrden.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoOrden estado = EstadoOrden.PENDIENTE;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_orden_equipo"))
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id",
            foreignKey = @ForeignKey(name = "fk_orden_tecnico"))
    private Tecnico tecnico;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant fechaCreacion = Instant.now();

    private Instant fechaAsignacion;
    private Instant fechaCierre;

    @OneToMany(mappedBy = "ordenServicio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleServicio> detalles = new ArrayList<>();
}
