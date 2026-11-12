package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.CancelacionNoPermitidaException;
import com.example.ecommerce.domain.exception.ClaveIdempotenciaInvalidaException;
import com.example.ecommerce.domain.exception.MotivoCancelacionInvalidoException;
import com.example.ecommerce.domain.exception.PagoInvalidoException;
import com.example.ecommerce.domain.exception.PagoNoConfirmadoException;
import com.example.ecommerce.domain.exception.PedidoSinDetallesException;
import com.example.ecommerce.domain.exception.TransicionEstadoInvalidaException;
import com.example.ecommerce.domain.valueobject.CambioEstadoPedido;
import com.example.ecommerce.domain.valueobject.DatosEntrega;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.EstadoPedido;
import com.example.ecommerce.domain.valueobject.NumeroPedido;
import com.example.ecommerce.domain.valueobject.PagoConfirmado;
import com.example.ecommerce.domain.valueobject.QuienCancela;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
 * <p>F-07 (gestión de estados):</p>
 * <ul>
 *   <li>RF-07.3 / RF-07.4: {@link #avanzarA} solo acepta el siguiente estado
 *       del diagrama (Figura 13); nada vuelve atrás.</li>
 *   <li>RF-07.5: {@link #cancelar} exige motivo y respeta quién puede cancelar
 *       en cada estado.</li>
 *   <li>RF-07.7: cada cambio queda en el historial (fecha, estado anterior,
 *       nuevo, quién y motivo). El registro del pedido es la primera entrada.</li>
 * </ul>
 *
 * <p>F-09 (confirmación del pago) / RN11:</p>
 * <ul>
 *   <li>Pendiente → Confirmado solo ocurre con {@link #confirmarPago}: el
 *       personal registra que el dinero llegó. {@link #avanzarA} no lo permite.</li>
 *   <li>RF-09.3: el monto recibido debe ser igual al total del pedido.</li>
 *   <li>RF-09.4: la fecha de recepción no puede ser futura ni anterior al
 *       día en que se registró el pedido (hora de Colombia).</li>
 * </ul>
 */
public class Pedido {

    /** Formato de la clave del intento de compra: un UUID que genera el navegador. */
    private static final Pattern FORMATO_CLAVE = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    /** RF-07.5: longitud máxima del motivo de cancelación (columna de 300). */
    public static final int MAX_MOTIVO_CANCELACION = 300;

    /** RF-09.4: las fechas del negocio se cuentan en la hora de Colombia. */
    private static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Bogota");

    private final long id;
    private final long clienteId;
    private final List<DetallePedido> detalles;
    private final DatosEntrega entrega;
    private final Instant creadoEn;
    private final String claveIdempotencia;
    private EstadoPedido estado;
    private final List<CambioEstadoPedido> historial;
    /** F-09: null mientras el pago no se haya confirmado. */
    private PagoConfirmado pago;

    /** Reconstruye un pedido sin historial cargado (pruebas y datos anteriores a F-07). */
    public Pedido(long id, long clienteId, List<DetallePedido> detalles, DatosEntrega entrega,
                  Instant creadoEn, String claveIdempotencia, EstadoPedido estado) {
        this(id, clienteId, detalles, entrega, creadoEn, claveIdempotencia, estado, List.of());
    }

    /** Reconstruye un pedido existente con su historial (lo usa la persistencia). */
    public Pedido(long id, long clienteId, List<DetallePedido> detalles, DatosEntrega entrega,
                  Instant creadoEn, String claveIdempotencia, EstadoPedido estado,
                  List<CambioEstadoPedido> historial) {
        this(id, clienteId, detalles, entrega, creadoEn, claveIdempotencia, estado, historial, null);
    }

    /** Reconstruye un pedido existente con su historial y, si lo tiene, su pago confirmado (F-09). */
    public Pedido(long id, long clienteId, List<DetallePedido> detalles, DatosEntrega entrega,
                  Instant creadoEn, String claveIdempotencia, EstadoPedido estado,
                  List<CambioEstadoPedido> historial, PagoConfirmado pago) {
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
        this.historial = new ArrayList<>(Objects.requireNonNull(historial, "historial"));
        this.pago = pago;
    }

    /**
     * RF-05.1: crea un pedido nuevo, todavía sin guardar (id = 0), en estado
     * Pendiente. RF-07.7: el registro queda como primera entrada del historial,
     * hecha por la propia clienta.
     */
    public static Pedido registrar(long clienteId, List<DetallePedido> detalles, DatosEntrega entrega,
                                   Instant ahora, String claveIdempotencia) {
        var registro = new CambioEstadoPedido(null, EstadoPedido.PENDIENTE, ahora, clienteId, null);
        return new Pedido(0L, clienteId, detalles, entrega, ahora, claveIdempotencia, EstadoPedido.PENDIENTE,
                List.of(registro));
    }

    /** Devuelve una copia con el id asignado por la base de datos. */
    public Pedido conId(long nuevoId) {
        return new Pedido(nuevoId, clienteId, detalles, entrega, creadoEn, claveIdempotencia, estado, historial,
                pago);
    }

    /**
     * RF-07.3 / RF-07.4: avanza el pedido al siguiente estado del flujo normal
     * (En proceso, En camino, Entregado, Completado). Ni la confirmación
     * (exige el pago, {@link #confirmarPago}) ni la cancelación (exige motivo,
     * {@link #cancelar}) pasan por aquí.
     *
     * @param nuevoEstado debe ser exactamente {@link EstadoPedido#siguiente()} del actual
     * @throws PagoNoConfirmadoException        si se intenta pasar a Confirmado sin registrar el pago (RN11)
     * @throws TransicionEstadoInvalidaException si el diagrama no permite ese paso
     */
    public void avanzarA(EstadoPedido nuevoEstado, long usuarioId, Instant ahora) {
        Objects.requireNonNull(nuevoEstado, "nuevoEstado");
        if (nuevoEstado == EstadoPedido.CONFIRMADO && estado == EstadoPedido.PENDIENTE) {
            throw new PagoNoConfirmadoException();
        }
        if (estado.siguiente().filter(nuevoEstado::equals).isEmpty()) {
            throw new TransicionEstadoInvalidaException(estado.name(), nuevoEstado.name());
        }
        registrarCambio(nuevoEstado, usuarioId, ahora, null);
    }

    /**
     * F-09 / RN11: el personal confirma que Lovely Girl recibió el dinero del
     * pedido. Solo desde Pendiente; el pedido pasa a Confirmado y el pago
     * queda guardado. La factura la emite el caso de uso en la misma
     * transacción (RF-09.5).
     *
     * @throws TransicionEstadoInvalidaException si el pedido ya no está Pendiente
     * @throws PagoInvalidoException             si el monto no es el total o la fecha no es válida
     */
    public void confirmarPago(PagoConfirmado pagoRecibido) {
        Objects.requireNonNull(pagoRecibido, "pagoRecibido");
        if (estado != EstadoPedido.PENDIENTE) {
            throw new TransicionEstadoInvalidaException(estado.name(), EstadoPedido.CONFIRMADO.name());
        }
        BigDecimal total = calcularTotal();
        if (pagoRecibido.monto().compareTo(total) != 0) {
            throw new PagoInvalidoException("El monto recibido ($ " + pagoRecibido.monto().toPlainString()
                    + ") no coincide con el total del pedido ($ " + total.toPlainString() + ").");
        }
        LocalDate diaPedido = LocalDate.ofInstant(creadoEn, ZONA_NEGOCIO);
        LocalDate hoy = LocalDate.ofInstant(pagoRecibido.confirmadoEn(), ZONA_NEGOCIO);
        if (pagoRecibido.fechaRecepcion().isAfter(hoy)) {
            throw new PagoInvalidoException("La fecha de recepción del pago no puede ser futura.");
        }
        if (pagoRecibido.fechaRecepcion().isBefore(diaPedido)) {
            throw new PagoInvalidoException("La fecha de recepción del pago no puede ser anterior al pedido.");
        }
        registrarCambio(EstadoPedido.CONFIRMADO, pagoRecibido.confirmadoPor(), pagoRecibido.confirmadoEn(), null);
        this.pago = pagoRecibido;
    }

    /**
     * RF-07.5: cancela el pedido con un motivo obligatorio. La clienta solo
     * puede cancelar mientras está Pendiente; la Administradora, mientras está
     * Pendiente o Confirmado. Después de eso el diagrama no admite cancelación.
     * La devolución del stock (RF-07.6) la hace el caso de uso, que es quien
     * tiene acceso al inventario.
     *
     * @throws TransicionEstadoInvalidaException  si el estado ya no admite cancelación
     * @throws CancelacionNoPermitidaException    si quien cancela no puede hacerlo en este estado
     * @throws MotivoCancelacionInvalidoException si falta el motivo o es demasiado largo
     */
    public void cancelar(QuienCancela quien, long usuarioId, String motivo, Instant ahora) {
        Objects.requireNonNull(quien, "quien");
        if (!estado.admiteCancelacion()) {
            throw new TransicionEstadoInvalidaException(estado.name(), EstadoPedido.CANCELADO.name());
        }
        if (!quien.puedeCancelarDesde(estado)) {
            throw new CancelacionNoPermitidaException(
                    "Solo la Administradora puede cancelar un pedido que ya está confirmado.");
        }
        registrarCambio(EstadoPedido.CANCELADO, usuarioId, ahora, validarMotivo(motivo));
    }

    private static String validarMotivo(String motivo) {
        String limpio = motivo == null ? "" : motivo.strip();
        if (limpio.isEmpty()) {
            throw new MotivoCancelacionInvalidoException("Escribe el motivo de la cancelación.");
        }
        if (limpio.length() > MAX_MOTIVO_CANCELACION) {
            throw new MotivoCancelacionInvalidoException(
                    "El motivo puede tener máximo " + MAX_MOTIVO_CANCELACION + " caracteres.");
        }
        return limpio;
    }

    private void registrarCambio(EstadoPedido nuevoEstado, long usuarioId, Instant ahora, String motivo) {
        historial.add(new CambioEstadoPedido(estado, nuevoEstado, Objects.requireNonNull(ahora, "ahora"),
                usuarioId, motivo));
        this.estado = nuevoEstado;
    }

    /** F-09: el pago confirmado, si ya se registró. */
    public Optional<PagoConfirmado> getPago() {
        return Optional.ofNullable(pago);
    }

    /** RF-07.5: motivo de la cancelación, si el pedido está cancelado. */
    public Optional<String> getMotivoCancelacion() {
        return historial.stream()
                .filter(cambio -> cambio.estadoNuevo() == EstadoPedido.CANCELADO)
                .map(CambioEstadoPedido::motivo)
                .reduce((primero, ultimo) -> ultimo);
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
    /** RF-07.7: historial de cambios, del más antiguo al más reciente. */
    public List<CambioEstadoPedido> getHistorial() { return List.copyOf(historial); }

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
