package com.example.ecommerce.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Modelo de persistencia de la tabla TOKENS_RECUPERACION (fechas en UTC).
 * Guarda solo la huella del token, nunca el token.
 */
@Entity
@Table(name = "TOKENS_RECUPERACION")
public class TokenRecuperacionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tokens_recuperacion_seq_gen")
    @SequenceGenerator(name = "tokens_recuperacion_seq_gen", sequenceName = "TOKENS_RECUPERACION_SEQ",
            allocationSize = 1)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 64, unique = true)
    private String huella;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(name = "usado_en")
    private LocalDateTime usadoEn;

    protected TokenRecuperacionJpaEntity() {
        // Requerido por JPA.
    }

    public TokenRecuperacionJpaEntity(Long id, Long usuarioId, String huella, LocalDateTime creadoEn,
                                      LocalDateTime expiraEn, LocalDateTime usadoEn) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.huella = huella;
        this.creadoEn = creadoEn;
        this.expiraEn = expiraEn;
        this.usadoEn = usadoEn;
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public String getHuella() { return huella; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public LocalDateTime getExpiraEn() { return expiraEn; }
    public LocalDateTime getUsadoEn() { return usadoEn; }
}
