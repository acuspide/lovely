package com.example.ecommerce.domain.valueobject;

import java.util.Optional;
import java.util.Set;

/**
 * Estados del pedido según el diagrama de estados del SyRS (Figura 13):
 *
 * <pre>
 * Pendiente → Confirmado → En proceso → En camino → Entregado → Completado
 *     ↓            ↓
 * Cancelado    Cancelado
 * </pre>
 *
 * <ul>
 *   <li>RF-07.3: solo se permiten las transiciones del diagrama.</li>
 *   <li>RF-07.4: nada vuelve atrás; Completado y Cancelado son finales.</li>
 * </ul>
 */
public enum EstadoPedido {

    /** Recién registrado por la clienta (RF-05.1). */
    PENDIENTE,
    /** El pago se confirmó. */
    CONFIRMADO,
    /** Se está preparando. */
    EN_PROCESO,
    /** Despachado (o, en retiro en tienda, listo para recoger). */
    EN_CAMINO,
    /** La clienta lo recibió. */
    ENTREGADO,
    /** Proceso cerrado (estado final). */
    COMPLETADO,
    /** Anulado, con motivo (estado final). */
    CANCELADO;

    /** Transiciones del diagrama (Figura 13). */
    public boolean puedeTransicionarA(EstadoPedido nuevoEstado) {
        return switch (this) {
            case PENDIENTE -> nuevoEstado == CONFIRMADO || nuevoEstado == CANCELADO;
            case CONFIRMADO -> nuevoEstado == EN_PROCESO || nuevoEstado == CANCELADO;
            case EN_PROCESO -> nuevoEstado == EN_CAMINO;
            case EN_CAMINO -> nuevoEstado == ENTREGADO;
            case ENTREGADO -> nuevoEstado == COMPLETADO;
            case COMPLETADO, CANCELADO -> false;
        };
    }

    /**
     * RF-07.3: el siguiente paso del flujo normal (sin contar la cancelación),
     * o vacío si el pedido ya está en un estado final.
     */
    public Optional<EstadoPedido> siguiente() {
        return switch (this) {
            case PENDIENTE -> Optional.of(CONFIRMADO);
            case CONFIRMADO -> Optional.of(EN_PROCESO);
            case EN_PROCESO -> Optional.of(EN_CAMINO);
            case EN_CAMINO -> Optional.of(ENTREGADO);
            case ENTREGADO -> Optional.of(COMPLETADO);
            case COMPLETADO, CANCELADO -> Optional.empty();
        };
    }

    /** RF-07.4: Completado y Cancelado no cambian más. */
    public boolean esFinal() {
        return this == COMPLETADO || this == CANCELADO;
    }

    /** RF-07.5: según el diagrama, solo se cancela desde Pendiente o Confirmado. */
    public boolean admiteCancelacion() {
        return puedeTransicionarA(CANCELADO);
    }

    /**
     * RF-08.1: el pedido ya pasó por Confirmado (y no se canceló), así que
     * tiene o debe tener factura.
     */
    public boolean yaFueConfirmado() {
        return this != PENDIENTE && this != CANCELADO;
    }

    /** RN17 / RF-02.9: estados en los que el pedido sigue vigente. */
    public static Set<EstadoPedido> activos() {
        return Set.of(PENDIENTE, CONFIRMADO, EN_PROCESO, EN_CAMINO, ENTREGADO, COMPLETADO);
    }
}
