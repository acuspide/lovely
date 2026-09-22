package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Contrato de entrada compartido por crear y editar (CU-01): mismos campos
 * en ambos flujos, solo cambia si se envía por POST o por PUT /{id}.
 */
public record GuardarArticuloRequest(
        @NotBlank(message = "El nombre es obligatorio.") String nombre,
        @NotBlank(message = "La descripción es obligatoria.") String descripcion,
        @NotNull(message = "El precio es obligatorio.") @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero.") BigDecimal precio,
        @NotNull(message = "La categoría es obligatoria.") CategoriaArticulo categoria,
        String marca,
        @NotBlank(message = "La imagen es obligatoria.") String imagenUrl,
        @NotNull(message = "El stock es obligatorio.") @Min(value = 0, message = "El stock no puede ser negativo.") Integer stock) {
}
