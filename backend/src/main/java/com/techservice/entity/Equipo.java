package com.techservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "equipos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_equipo_serie", columnNames = "numero_serie")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_equipo_cliente"))
    private Cliente cliente;

    @Column(nullable = false, length = 80)
    private String tipo;

    @Column(nullable = false, length = 80)
    private String marca;

    @Column(nullable = false, length = 80)
    private String modelo;

    @Column(name = "numero_serie", nullable = false, length = 100)
    private String numeroSerie;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant creadoEn = Instant.now();
}
