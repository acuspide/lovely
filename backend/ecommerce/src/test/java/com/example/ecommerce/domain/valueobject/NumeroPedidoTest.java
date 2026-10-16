package com.example.ecommerce.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** RF-05.2: formato del número de pedido. */
class NumeroPedidoTest {

    @Test
    void tieneElPrefijoYSeisDigitos() {
        assertEquals("LG-000001", new NumeroPedido(1).valor());
        assertEquals("LG-001234", new NumeroPedido(1234).valor());
    }

    @Test
    void crecePasadoElMillon() {
        assertEquals("LG-1000000", new NumeroPedido(1_000_000).valor());
    }

    @Test
    void elConsecutivoDebeSerPositivo() {
        assertThrows(IllegalArgumentException.class, () -> new NumeroPedido(0));
    }
}
