package com.example.ecommerce.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Modelo de persistencia de la tabla FACTURAS (F-08). Fechas en UTC. */
@Entity
@Table(name = "FACTURAS")
public class FacturaJpaEntity {

    /** RF-08.2: la secuencia sin caché da el consecutivo del número de factura. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "facturas_seq_gen")
    @SequenceGenerator(name = "facturas_seq_gen", sequenceName = "FACTURAS_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "emitida_en", nullable = false)
    private LocalDateTime emitidaEn;

    @Column(nullable = false, length = 10)
    private String estado;

    @Column(name = "anulada_en")
    private LocalDateTime anuladaEn;

    @Column(name = "nombre_clienta", nullable = false, length = 120)
    private String nombreClienta;

    @Column(name = "email_clienta", nullable = false, length = 180)
    private String emailClienta;

    @Column(name = "telefono_clienta", length = 10)
    private String telefonoClienta;

    @Column(name = "tasa_iva", nullable = false, precision = 5, scale = 4)
    private BigDecimal tasaIva;

    protected FacturaJpaEntity() {
        // Requerido por JPA.
    }

    public FacturaJpaEntity(Long id, Long pedidoId, LocalDateTime emitidaEn, String estado, LocalDateTime anuladaEn,
                            String nombreClienta, String emailClienta, String telefonoClienta, BigDecimal tasaIva) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.emitidaEn = emitidaEn;
        this.estado = estado;
        this.anuladaEn = anuladaEn;
        this.nombreClienta = nombreClienta;
        this.emailClienta = emailClienta;
        this.telefonoClienta = telefonoClienta;
        this.tasaIva = tasaIva;
    }

    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public LocalDateTime getEmitidaEn() { return emitidaEn; }
    public String getEstado() { return estado; }
    public LocalDateTime getAnuladaEn() { return anuladaEn; }
    public String getNombreClienta() { return nombreClienta; }
    public String getEmailClienta() { return emailClienta; }
    public String getTelefonoClienta() { return telefonoClienta; }
    public BigDecimal getTasaIva() { return tasaIva; }
}
