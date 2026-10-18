package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.domain.valueobject.TipoEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Patrón: DTO — lo que envía la clienta al confirmar su pedido (F-05). Igual
 * que en el carrito, solo trae productos y cantidades: precios y stock los
 * pone el backend. La clienta sale de la sesión, nunca del cuerpo.
 *
 * @param claveIdempotencia RF-05.7: identificador único del intento de compra (UUID del navegador)
 */
public record RegistrarPedidoRequest(
        @NotBlank(message = "Falta el identificador del intento de compra.") String claveIdempotencia,
        @NotEmpty(message = "Tu carrito está vacío.")
        @Size(max = 50, message = "El pedido puede tener como máximo 50 productos distintos.")
        List<@Valid @NotNull Linea> lineas,
        @NotNull(message = "Faltan los datos de entrega.") @Valid Entrega entrega) {

    public record Linea(
            @NotNull(message = "Falta el producto.") Long articuloId,
            @NotNull(message = "Falta la cantidad.")
            @Min(value = 1, message = "La cantidad debe ser mayor que cero.")
            @Max(value = 999, message = "La cantidad máxima por producto es 999.") Integer cantidad) {
    }

    /** RF-04.6: las reglas completas las aplica el value object DatosEntrega del dominio. */
    public record Entrega(
            @NotNull(message = "Elige cómo quieres recibir tu pedido.") TipoEntrega tipo,
            String nombreContacto,
            String telefono,
            String direccion,
            String ciudad,
            String indicaciones) {
    }
}
