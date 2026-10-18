package com.example.ecommerce.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Modelo de persistencia de la tabla SESIONES (fechas en UTC). La traducción
 * con la entidad de dominio Sesion la hace SesionRepositoryJpaAdapter.
 */
@Entity
@Table(name = "SESIONES")
public class SesionJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "iniciada_en", nullable = false)
    private LocalDateTime iniciadaEn;

    @Column(name = "ultima_actividad", nullable = false)
    private LocalDateTime ultimaActividad;

    @Column(name = "cerrada_en")
    private LocalDateTime cerradaEn;

    protected SesionJpaEntity() {
        // Requerido por JPA.
    }

    public SesionJpaEntity(String id, Long usuarioId, LocalDateTime iniciadaEn,
                           LocalDateTime ultimaActividad, LocalDateTime cerradaEn) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.iniciadaEn = iniciadaEn;
        this.ultimaActividad = ultimaActividad;
        this.cerradaEn = cerradaEn;
    }

    public String getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public LocalDateTime getIniciadaEn() { return iniciadaEn; }
    public LocalDateTime getUltimaActividad() { return ultimaActividad; }
    public LocalDateTime getCerradaEn() { return cerradaEn; }
}
