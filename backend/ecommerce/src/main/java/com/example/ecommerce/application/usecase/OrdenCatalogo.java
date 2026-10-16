package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;

import java.util.Comparator;

/**
 * RF-03.6: formas de ordenar el catálogo que puede elegir la clienta.
 *
 * <p>Patrón: Strategy — cada opción trae su propio criterio de comparación, así
 * que agregar un orden nuevo (por ejemplo "más vendidos") es agregar una
 * constante aquí, sin tocar el caso de uso.</p>
 */
public enum OrdenCatalogo {

    /** Alfabético por nombre (sin distinguir mayúsculas ni tildes). Es el orden por defecto. */
    NOMBRE(Comparator.comparing(articulo -> articulo.getNombre().clave())),

    /** Precio de menor a mayor. */
    PRECIO_MENOR(Comparator.comparing(articulo -> articulo.getPrecio().valor())),

    /** Precio de mayor a menor. */
    PRECIO_MAYOR(Comparator.<Articulo, java.math.BigDecimal>comparing(articulo -> articulo.getPrecio().valor()).reversed());

    private final Comparator<Articulo> criterio;

    OrdenCatalogo(Comparator<Articulo> criterio) {
        this.criterio = criterio;
    }

    /** Criterio de la opción; a igualdad de precio desempata por nombre para que el orden sea estable. */
    public Comparator<Articulo> comparador() {
        return criterio.thenComparing(NOMBRE.criterio);
    }
}
