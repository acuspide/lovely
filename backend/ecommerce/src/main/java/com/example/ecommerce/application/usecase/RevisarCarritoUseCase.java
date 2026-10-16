package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.exception.CarritoDemasiadoGrandeException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.valueobject.LineaCarritoRevisada;
import com.example.ecommerce.domain.valueobject.RevisionCarrito;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * F-04: revisa el carrito de la clienta contra el inventario actual.
 *
 * <ul>
 *   <li>RF-04.1 / CO-01: verifica en tiempo real que cada producto exista,
 *       esté activo y tenga stock (sin modificar el inventario).</li>
 *   <li>RF-04.2 / CU-02 A2: si la cantidad supera el stock, la ajusta al máximo.</li>
 *   <li>RF-04.7 / CU-02 paso 10 y A3: antes de confirmar, la misma revisión
 *       detecta lo que se agotó entre la selección y la confirmación.</li>
 * </ul>
 *
 * <p>No requiere sesión: el CU-02 permite armar el carrito como invitada.</p>
 */
public class RevisarCarritoUseCase {

    /** Tope de productos distintos por revisión; un carrito real de boutique queda muy por debajo. */
    public static final int MAXIMO_PRODUCTOS_DISTINTOS = 50;

    private final ArticuloRepository articuloRepository;

    public RevisarCarritoUseCase(ArticuloRepository articuloRepository) {
        this.articuloRepository = articuloRepository;
    }

    /**
     * @param lineas productos y cantidades que envía el navegador. Si un
     *               producto viene repetido, se suman sus cantidades.
     */
    public RevisionCarrito ejecutar(List<LineaSolicitada> lineas) {
        Map<Long, Integer> cantidadPorArticulo = new LinkedHashMap<>();
        for (LineaSolicitada linea : lineas) {
            cantidadPorArticulo.merge(linea.articuloId(), linea.cantidad(), Integer::sum);
        }
        if (cantidadPorArticulo.size() > MAXIMO_PRODUCTOS_DISTINTOS) {
            throw new CarritoDemasiadoGrandeException(MAXIMO_PRODUCTOS_DISTINTOS);
        }

        List<LineaCarritoRevisada> revisadas = cantidadPorArticulo.entrySet().stream()
                .map(entrada -> LineaCarritoRevisada.revisar(
                        entrada.getKey(),
                        articuloRepository.obtenerPorId(entrada.getKey()),
                        entrada.getValue()))
                .toList();

        return new RevisionCarrito(revisadas);
    }

    /** Lo que el navegador sabe de cada línea: producto y cantidad. */
    public record LineaSolicitada(long articuloId, int cantidad) {
    }
}
