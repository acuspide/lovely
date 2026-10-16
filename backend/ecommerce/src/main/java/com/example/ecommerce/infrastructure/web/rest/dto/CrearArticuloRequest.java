package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Patrón: DTO — datos de entrada para registrar un producto (RF-02.1, CU-01
 * "Crear Producto"). Es el único momento en que se fija el stock: el stock
 * inicial. Después, el stock solo cambia por el módulo de inventario (RF-02.6).
 *
 * <p>Estas validaciones dan una respuesta rápida al formulario; las reglas de
 * negocio de verdad (RF-02.2) viven en el dominio y se aplican igual aunque
 * la petición no pase por aquí.</p>
 */
public record CrearArticuloRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres.") String nombre,
        @NotBlank(message = "La descripción es obligatoria.")
        @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres.") String descripcion,
        @NotNull(message = "El precio es obligatorio.")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero.") BigDecimal precio,
        @NotNull(message = "La categoría es obligatoria.") CategoriaArticulo categoria,
        @Size(max = 100, message = "La marca no puede superar 100 caracteres.") String marca,
        // RF-02.4 / CU-01 A4: opcional; viene de POST /api/articulos/imagenes.
        @Size(max = 500, message = "La URL de la imagen es demasiado larga.") String imagenUrl,
        @NotNull(message = "El stock inicial es obligatorio.")
        @Min(value = 0, message = "El stock inicial no puede ser negativo.") Integer stockInicial) {
}
