package com.example.ecommerce.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Modelo de persistencia de la tabla DETALLES_PEDIDO (RF-05.3). */
@Entity
@Table(name = "DETALLES_PEDIDO")
public class DetallePedidoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "detalles_pedido_seq_gen")
    @SequenceGenerator(name = "detalles_pedido_seq_gen", sequenceName = "DETALLES_PEDIDO_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "articulo_id", nullable = false)
    private Long articuloId;

    @Column(name = "nombre_articulo", nullable = false, length = 150)
    private String nombreArticulo;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    protected DetallePedidoJpaEntity() {
        // Requerido por JPA.
    }

    public DetallePedidoJpaEntity(Long articuloId, String nombreArticulo, int cantidad, BigDecimal precioUnitario) {
        this.articuloId = articuloId;
        this.nombreArticulo = nombreArticulo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Long getId() { return id; }
    public Long getArticuloId() { return articuloId; }
    public String getNombreArticulo() { return nombreArticulo; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
}
