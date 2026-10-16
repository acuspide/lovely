package com.example.ecommerce.domain.valueobject;

import java.time.Instant;
import java.util.Objects;

/**
 * Patrón: Value Object — una entrada del historial del pedido (RF-07.7):
 * qué estado tenía, a cuál pasó, cuándo, quién lo hizo y por qué (el motivo
 * solo aplica a la cancelación).
 *
 * @param estadoAnterior null en la primera entrada (el registro del pedido)
 * @param usuarioId      cuenta que hizo el cambio (clienta o personal)
 * @param motivo         RF-07.5: obligatorio al cancelar; null en los demás cambios
 */
public record CambioEstadoPedido(EstadoPedido estadoAnterior, EstadoPedido estadoNuevo, Instant fecha,
                                 long usuarioId, String motivo) {

    public CambioEstadoPedido {
        Objects.requireNonNull(estadoNuevo, "estadoNuevo");
        Objects.requireNonNull(fecha, "fecha");
    }
}
