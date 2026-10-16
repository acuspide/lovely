package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.ClaveIdempotenciaInvalidaException;
import com.example.ecommerce.domain.exception.PedidoSinDetallesException;
import com.example.ecommerce.domain.exception.TransicionEstadoInvalidaException;
import com.example.ecommerce.domain.valueobject.DatosEntrega;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.EstadoPedido;
import com.example.ecommerce.domain.valueobject.NumeroPedido;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Pedido de una clienta (F-05, RF-05).
 *
 * <ul>
 *   <li>RF-05.1: nace en estado "Pendiente", con su fecha y hora de creación.</li>
 *   <li>RF-05.2: su número visible ({@link NumeroPedido}) sale del id que
 *       asigna la base de datos.</li>
 *   <li>RF-05.3 / RN16 / RN21: pertenece a una sola clienta y tiene al menos
 *       un producto, con su precio del momento y los datos de entrega.</li>
 *   <li>RF-05.7: guarda la clave del intento de compra, para no registrar el
 *       mismo pedido dos veces si la clienta reintenta.</li>
 * </ul>
 *
 * <p>Los cambios de estado posteriores (Confirmado, En proceso…) son F-07.</p>
 */
public class Pedido {

    /** Formato de la clave del intento de compra: un UUID que genera el navegador. */
    private static final Pattern FORMATO_CLAVE = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final long id;
    private final long clienteId;
    private final List<DetallePedido> detalles;
    private final DatosEntrega entrega;
    private final Instant creadoEn;
    private final String claveIdempotencia;
    private EstadoPedido estado;

    /** Reconstruye un pedido existente (lo usa la persistencia). */
    public Pedido(long id, long clienteId, List<DetallePedido> detalles, DatosEntrega entrega,
                  Instant creadoEn, String claveIdempotencia, EstadoPedido estado) {
        if (detalles == null || detalles.isEmpty()) {
            throw new PedidoSinDetallesException();
        }
        if (claveIdempotencia == null || !FORMATO_CLAVE.matcher(claveIdempotencia).matches()) {
            throw new ClaveIdempotenciaInvalidaException();
        }
        this.id = id;
        this.clienteId = clienteId;
        this.detalles = List.copyOf(detalles);
        this.entrega = Objects.requireNonNull(entrega, "entrega");
        this.creadoEn = Objects.requireNonNull(creadoEn, "creadoEn");
        this.claveIdempotencia = claveIdempotencia.toLowerCase();
        this.estado = Objects.requireNonNull(estado, "estado");
    }

    /**
     * RF-05.1: crea un pedido nuevo, todavía sin guardar (id = 0), en estado Pendiente.
     */
    public static Pedido registrar(long clienteId, List<DetallePedido> detalles, DatosEntrega entrega,
                                   Instant ahora, String claveIdempotencia) {
        return new Pedido(0L, clienteId, detalles, entrega, ahora, claveIdempotencia, EstadoPedido.PENDIENTE);
    }

    /** Devuelve una copia con el id asignado por la base de datos. */
    public Pedido conId(long nuevoId) {
        return new Pedido(nuevoId, clienteId, detalles, entrega, creadoEn, claveIdempotencia, estado);
    }

    /** Cambio de estado validado contra el diagrama de estados (la gestión completa es F-07). */
    public void confirmar() {
        transicionarA(EstadoPedido.CONFIRMADO);
    }

    public void cancelar() {
        transicionarA(EstadoPedido.CANCELADO);
    }

    private void transicionarA(EstadoPedido nuevoEstado) {
        if (!estado.puedeTransicionarA(nuevoEstado)) {
            throw new TransicionEstadoInvalidaException(estado.name(), nuevoEstado.name());
        }
        this.estado = nuevoEstado;
    }

    /** RN17 / RF-02.9: un pedido está activo mientras no se cancele. */
    public boolean estaActivo() {
        return estado != EstadoPedido.CANCELADO;
    }

    public boolean incluyeArticulo(long articuloId) {
        return detalles.stream().anyMatch(detalle -> detalle.getArticuloId() == articuloId);
    }

    public BigDecimal calcularTotal() {
        return detalles.stream()
                .map(DetallePedido::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int cantidadDeUnidades() {
        return detalles.stream().mapToInt(DetallePedido::getCantidad).sum();
    }

    /** RF-05.2: número visible; solo existe cuando el pedido ya se guardó. */
    public NumeroPedido getNumero() {
        return new NumeroPedido(id);
    }

    public long getId() { return id; }
    public long getClienteId() { return clienteId; }
    public EstadoPedido getEstado() { return estado; }
    public List<DetallePedido> getDetalles() { return detalles; }
    public DatosEntrega getEntrega() { return entrega; }
    public Instant getCreadoEn() { return creadoEn; }
    public String getClaveIdempotencia() { return claveIdempotencia; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pedido otro)) return false;
        return id == otro.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
