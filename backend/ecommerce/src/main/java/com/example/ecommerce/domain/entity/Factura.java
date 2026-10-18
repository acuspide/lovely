package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.valueobject.DesgloseIva;
import com.example.ecommerce.domain.valueobject.EstadoFactura;
import com.example.ecommerce.domain.valueobject.NumeroFactura;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Factura de venta de un pedido (F-08).
 *
 * <ul>
 *   <li>RF-08.1: se emite cuando el pedido pasa a Confirmado.</li>
 *   <li>RF-08.2 / RN02 / RN15: pertenece a un único pedido y su número es
 *       consecutivo ({@link NumeroFactura}).</li>
 *   <li>RF-08.3: guarda una copia de los datos de la clienta y la tasa de IVA
 *       del momento, para que el PDF salga siempre igual aunque luego cambien
 *       la cuenta o la tasa. Los productos y precios ya quedan fijos en el
 *       pedido (RF-05.3).</li>
 *   <li>RF-08.6: si el pedido se cancela, la factura se anula, no se borra.</li>
 * </ul>
 */
public class Factura {

    private final long id;
    private final long pedidoId;
    private final Instant emitidaEn;
    private final String nombreClienta;
    private final String emailClienta;
    private final String telefonoClienta;
    private final BigDecimal tasaIva;
    private EstadoFactura estado;
    private Instant anuladaEn;

    /** Reconstruye una factura existente (lo usa la persistencia). */
    public Factura(long id, long pedidoId, Instant emitidaEn, String nombreClienta, String emailClienta,
                   String telefonoClienta, BigDecimal tasaIva, EstadoFactura estado, Instant anuladaEn) {
        if (pedidoId <= 0) {
            // RN02: no hay factura sin un pedido previo.
            throw new IllegalArgumentException("La factura debe pertenecer a un pedido registrado.");
        }
        DesgloseIva.validarTasa(tasaIva);
        this.id = id;
        this.pedidoId = pedidoId;
        this.emitidaEn = Objects.requireNonNull(emitidaEn, "emitidaEn");
        this.nombreClienta = Objects.requireNonNull(nombreClienta, "nombreClienta");
        this.emailClienta = Objects.requireNonNull(emailClienta, "emailClienta");
        this.telefonoClienta = telefonoClienta;
        this.tasaIva = tasaIva;
        this.estado = Objects.requireNonNull(estado, "estado");
        this.anuladaEn = anuladaEn;
    }

    /** RF-08.1: nueva factura emitida, todavía sin número (id = 0). */
    public static Factura emitir(long pedidoId, String nombreClienta, String emailClienta, String telefonoClienta,
                                 BigDecimal tasaIva, Instant ahora) {
        return new Factura(0L, pedidoId, ahora, nombreClienta, emailClienta, telefonoClienta, tasaIva,
                EstadoFactura.EMITIDA, null);
    }

    public Factura conId(long nuevoId) {
        return new Factura(nuevoId, pedidoId, emitidaEn, nombreClienta, emailClienta, telefonoClienta, tasaIva,
                estado, anuladaEn);
    }

    /** RF-08.6: anula la factura (idempotente: anular dos veces no cambia nada). */
    public void anular(Instant ahora) {
        if (estado == EstadoFactura.EMITIDA) {
            this.estado = EstadoFactura.ANULADA;
            this.anuladaEn = Objects.requireNonNull(ahora, "ahora");
        }
    }

    public boolean estaAnulada() {
        return estado == EstadoFactura.ANULADA;
    }

    /** RF-08.3: base, IVA y total del pedido facturado, con la tasa guardada en la factura. */
    public DesgloseIva desglosar(Pedido pedido) {
        if (pedido.getId() != pedidoId) {
            throw new IllegalArgumentException("El pedido no corresponde a esta factura.");
        }
        return DesgloseIva.deTotalConIva(pedido.calcularTotal(), tasaIva);
    }

    public NumeroFactura getNumero() {
        return new NumeroFactura(id);
    }

    public long getId() { return id; }
    public long getPedidoId() { return pedidoId; }
    public Instant getEmitidaEn() { return emitidaEn; }
    public String getNombreClienta() { return nombreClienta; }
    public String getEmailClienta() { return emailClienta; }
    public Optional<String> getTelefonoClienta() { return Optional.ofNullable(telefonoClienta); }
    public BigDecimal getTasaIva() { return tasaIva; }
    public EstadoFactura getEstado() { return estado; }
    public Optional<Instant> getAnuladaEn() { return Optional.ofNullable(anuladaEn); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Factura otra)) return false;
        return id == otra.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
