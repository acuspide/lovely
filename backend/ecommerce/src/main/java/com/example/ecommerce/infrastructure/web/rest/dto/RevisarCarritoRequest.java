package com.example.ecommerce.infrastructure.web.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Patrón: DTO — el carrito que envía el navegador para revisarlo (F-04). Solo
 * trae producto y cantidad: el precio y el stock los pone el backend.
 */
public record RevisarCarritoRequest(
        @NotNull(message = "El carrito es obligatorio.")
        @Size(max = 50, message = "El carrito puede tener como máximo 50 productos distintos.")
        List<@Valid @NotNull Linea> lineas) {

    /** RN19: la cantidad es un entero mayor a cero. */
    public record Linea(
            @NotNull(message = "Falta el producto.") Long articuloId,
            @NotNull(message = "Falta la cantidad.")
            @Min(value = 1, message = "La cantidad debe ser mayor que cero.")
            @Max(value = 999, message = "La cantidad máxima por producto es 999.") Integer cantidad) {
    }
}
