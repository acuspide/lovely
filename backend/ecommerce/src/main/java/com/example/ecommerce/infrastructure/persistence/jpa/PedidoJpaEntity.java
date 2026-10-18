package com.example.ecommerce.infrastructure.persistence.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de persistencia de la tabla PEDIDOS (F-05). Fechas en UTC. Los datos
 * de entrega van en columnas del mismo pedido porque siempre se leen juntos.
 */
@Entity
@Table(name = "PEDIDOS")
public class PedidoJpaEntity {

    /** RF-05.2: la secuencia sin caché da el consecutivo del número de pedido. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pedidos_seq_gen")
    @SequenceGenerator(name = "pedidos_seq_gen", sequenceName = "PEDIDOS_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "clave_idempotencia", nullable = false, length = 36)
    private String claveIdempotencia;

    @Column(name = "tipo_entrega", nullable = false, length = 20)
    private String tipoEntrega;

    @Column(name = "nombre_contacto", nullable = false, length = 120)
    private String nombreContacto;

    @Column(name = "telefono_contacto", nullable = false, length = 10)
    private String telefonoContacto;

    @Column(length = 200)
    private String direccion;

    @Column(length = 80)
    private String ciudad;

    @Column(length = 300)
    private String indicaciones;

    /*
     * Detalles e historial se cargan con el pedido, pero cada colección en su
     * propia consulta (SUBSELECT): dos listas EAGER con JOIN no se pueden
     * combinar en Hibernate y, en las listas de pedidos, SUBSELECT evita una
     * consulta por pedido.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "pedido_id", nullable = false)
    @OrderBy("id")
    private List<DetallePedidoJpaEntity> detalles = new ArrayList<>();

    /** RF-07.7: historial de estados, del más antiguo al más reciente. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "pedido_id", nullable = false)
    @OrderBy("id")
    private List<HistorialEstadoPedidoJpaEntity> historial = new ArrayList<>();

    protected PedidoJpaEntity() {
        // Requerido por JPA.
    }

    public PedidoJpaEntity(Long id, Long clienteId, String estado, LocalDateTime creadoEn, String claveIdempotencia,
                           String tipoEntrega, String nombreContacto, String telefonoContacto, String direccion,
                           String ciudad, String indicaciones, List<DetallePedidoJpaEntity> detalles,
                           List<HistorialEstadoPedidoJpaEntity> historial) {
        this.id = id;
        this.clienteId = clienteId;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.claveIdempotencia = claveIdempotencia;
        this.tipoEntrega = tipoEntrega;
        this.nombreContacto = nombreContacto;
        this.telefonoContacto = telefonoContacto;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.indicaciones = indicaciones;
        this.detalles = new ArrayList<>(detalles);
        this.historial = new ArrayList<>(historial);
    }

    /**
     * F-07: aplica un cambio de estado a un pedido ya guardado. Los detalles no
     * se tocan (no cambian después del registro) y el historial solo crece:
     * se agregan las entradas nuevas al final.
     */
    public void actualizarEstado(String nuevoEstado, List<HistorialEstadoPedidoJpaEntity> entradasNuevas) {
        this.estado = nuevoEstado;
        this.historial.addAll(entradasNuevas);
    }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public String getEstado() { return estado; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public String getClaveIdempotencia() { return claveIdempotencia; }
    public String getTipoEntrega() { return tipoEntrega; }
    public String getNombreContacto() { return nombreContacto; }
    public String getTelefonoContacto() { return telefonoContacto; }
    public String getDireccion() { return direccion; }
    public String getCiudad() { return ciudad; }
    public String getIndicaciones() { return indicaciones; }
    public List<DetallePedidoJpaEntity> getDetalles() { return detalles; }
    public List<HistorialEstadoPedidoJpaEntity> getHistorial() { return historial; }
}
