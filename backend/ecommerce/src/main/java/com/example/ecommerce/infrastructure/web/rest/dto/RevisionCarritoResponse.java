package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.valueobject.LineaCarritoRevisada;
import com.example.ecommerce.domain.valueobject.RevisionCarrito;

import java.math.BigDecimal;
import java.util.List;

/**
 * Patrón: DTO — resultado de revisar el carrito (F-04). Cada línea trae los
 * datos actuales del producto y, si cambió algo, un mensaje listo para mostrar
 * a la clienta.
 */
public record RevisionCarritoResponse(
        List<LineaResponse> lineas,
        BigDecimal total,
        int unidades,
        boolean requiereAtencion,
        boolean tieneProductosComprables) {

    public static RevisionCarritoResponse desde(RevisionCarrito revision) {
        return new RevisionCarritoResponse(
                revision.getLineas().stream().map(LineaResponse::desde).toList(),
                revision.total(),
                revision.cantidadDeUnidades(),
                revision.requiereAtencion(),
                revision.tieneProductosComprables());
    }

    public record LineaResponse(
            long articuloId,
            String nombre,
            String imagenUrl,
            String categoria,
            String marca,
            BigDecimal precio,
            int stockDisponible,
            int cantidadSolicitada,
            int cantidad,
            BigDecimal subtotal,
            String estado,
            String mensaje) {

        static LineaResponse desde(LineaCarritoRevisada linea) {
            Articulo articulo = linea.getArticulo().orElse(null);
            return new LineaResponse(
                    linea.getArticuloId(),
                    articulo != null ? articulo.getNombre().getValor() : "Producto no disponible",
                    articulo != null ? articulo.getImagenUrl() : null,
                    articulo != null ? articulo.getCategoria().name() : null,
                    articulo != null ? articulo.getMarca() : null,
                    articulo != null ? articulo.getPrecio().valor() : BigDecimal.ZERO,
                    articulo != null && articulo.isActivo() ? articulo.getStock() : 0,
                    linea.getCantidadSolicitada(),
                    linea.getCantidad(),
                    linea.subtotal(),
                    linea.getEstado().name(),
                    mensaje(linea, articulo));
        }

        /** Texto para la clienta (CU-02, cursos alternos A1, A2 y A3). */
        private static String mensaje(LineaCarritoRevisada linea, Articulo articulo) {
            String nombre = articulo != null ? "«" + articulo.getNombre().getValor() + "»" : "Un producto";
            return switch (linea.getEstado()) {
                case DISPONIBLE -> null;
                case AJUSTADA -> "Solo quedan " + linea.getCantidad() + " unidades de " + nombre
                        + ". Ajustamos la cantidad.";
                case AGOTADO -> nombre + " se agotó y no se puede comprar por ahora.";
                case NO_DISPONIBLE -> nombre + " ya no está disponible en el catálogo.";
            };
        }
    }
}
