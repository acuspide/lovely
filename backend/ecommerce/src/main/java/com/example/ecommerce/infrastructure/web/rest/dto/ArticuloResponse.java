package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.domain.entity.Articulo;

import java.math.BigDecimal;

/** Patrón: DTO — vista pública y segura de un Articulo (usada por el catálogo y el panel admin). */
public record ArticuloResponse(
        long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        String categoria,
        String marca,
        String imagenUrl,
        int stock,
        boolean agotado) {

    public static ArticuloResponse desde(Articulo articulo) {
        return new ArticuloResponse(
                articulo.getId(),
                articulo.getNombre().getValor(),
                articulo.getDescripcion(),
                articulo.getPrecio().valor(),
                articulo.getCategoria().name(),
                articulo.getMarca(),
                articulo.getImagenUrl(),
                articulo.getStock(),
                articulo.estaAgotado());
    }
}
