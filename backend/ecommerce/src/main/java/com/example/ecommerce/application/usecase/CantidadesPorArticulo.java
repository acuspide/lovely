package com.example.ecommerce.application.usecase;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * Refactorización H-02: regla única de "si un producto viene repetido, se
 * suman sus cantidades". La comparten el registro del pedido (F-05), la
 * revisión del carrito (F-04) y la cancelación del pedido (F-07), que antes
 * repetían el mismo bucle sobre tres tipos de línea distintos.
 *
 * <p>Es de visibilidad de paquete: no amplía la API pública de la capa de
 * aplicación. No está en el dominio porque trabaja sobre los tipos de
 * solicitud de los casos de uso.</p>
 */
final class CantidadesPorArticulo {

    private CantidadesPorArticulo() {
    }

    /**
     * @param lineas   líneas en el orden en que llegaron
     * @param articulo cómo obtener el id del producto de cada línea
     * @param cantidad cómo obtener la cantidad de cada línea
     * @return id del producto → cantidad total, en el orden de primera aparición
     */
    static <T> Map<Long, Integer> sumar(Collection<T> lineas, ToLongFunction<T> articulo, ToIntFunction<T> cantidad) {
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (T linea : lineas) {
            cantidades.merge(articulo.applyAsLong(linea), cantidad.applyAsInt(linea), Integer::sum);
        }
        return cantidades;
    }
}
