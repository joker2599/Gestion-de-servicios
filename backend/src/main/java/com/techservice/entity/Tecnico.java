package com.techservice.entity;

import com.techservice.enums.EstadoDisponibilidad;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tecnicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_tecnico_usuario"))
    private Usuario usuario;

    @Column(nullable = false, length = 120)
    private String especialidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoDisponibilidad disponibilidad = EstadoDisponibilidad.DISPONIBLE;

    @Column(length = 30)
    private String telefono;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant creadoEn = Instant.now();
}
