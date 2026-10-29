package com.example.ecommerce.application.usecase;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Refactorización H-02: la suma de cantidades por producto vive en un solo lugar. */
public class CantidadesPorArticuloTest {

    private record Linea(long articuloId, int cantidad) {
    }

    @Test
    void sumaLasCantidadesDeUnProductoRepetido() {
        Map<Long, Integer> resultado = CantidadesPorArticulo.sumar(
                List.of(new Linea(7L, 2), new Linea(3L, 1), new Linea(7L, 4)),
                Linea::articuloId, Linea::cantidad);

        assertEquals(Map.of(7L, 6, 3L, 1), resultado);
    }

    @Test
    void conservaElOrdenDePrimeraAparicion() {
        // RF-05.3: los detalles del pedido siguen el orden del carrito
        Map<Long, Integer> resultado = CantidadesPorArticulo.sumar(
                List.of(new Linea(9L, 1), new Linea(2L, 1), new Linea(5L, 1), new Linea(2L, 1)),
                Linea::articuloId, Linea::cantidad);

        assertEquals(List.of(9L, 2L, 5L), List.copyOf(resultado.keySet()));
    }

    @Test
    void sinLineasDevuelveUnMapaVacio() {
        assertTrue(CantidadesPorArticulo.sumar(List.<Linea>of(), Linea::articuloId, Linea::cantidad).isEmpty());
    }
}
