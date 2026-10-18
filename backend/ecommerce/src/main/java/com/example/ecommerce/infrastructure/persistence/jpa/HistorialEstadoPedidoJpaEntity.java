package com.example.ecommerce.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Modelo de persistencia de la tabla HISTORIAL_ESTADOS_PEDIDO (RF-07.7). Fechas en UTC. */
@Entity
@Table(name = "HISTORIAL_ESTADOS_PEDIDO")
public class HistorialEstadoPedidoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "historial_estados_pedido_seq_gen")
    @SequenceGenerator(name = "historial_estados_pedido_seq_gen", sequenceName = "HISTORIAL_ESTADOS_PEDIDO_SEQ",
            allocationSize = 1)
    private Long id;

    @Column(name = "estado_anterior", length = 20)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private String estadoNuevo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(length = 300)
    private String motivo;

    protected HistorialEstadoPedidoJpaEntity() {
        // Requerido por JPA.
    }

    public HistorialEstadoPedidoJpaEntity(String estadoAnterior, String estadoNuevo, LocalDateTime fecha,
                                          Long usuarioId, String motivo) {
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.fecha = fecha;
        this.usuarioId = usuarioId;
        this.motivo = motivo;
    }

    public Long getId() { return id; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public LocalDateTime getFecha() { return fecha; }
    public Long getUsuarioId() { return usuarioId; }
    public String getMotivo() { return motivo; }
}
