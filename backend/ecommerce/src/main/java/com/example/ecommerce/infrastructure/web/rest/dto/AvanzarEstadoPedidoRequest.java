package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.domain.valueobject.EstadoPedido;
import jakarta.validation.constraints.NotNull;

/**
 * RF-07.3: estado al que se quiere llevar el pedido. Debe ser el siguiente
 * del diagrama; si otra persona ya lo cambió, se responde 409.
 */
public record AvanzarEstadoPedidoRequest(@NotNull(message = "Debes indicar el nuevo estado.") EstadoPedido estado) {
}
