package com.example.ecommerce.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** RF-08.3: base + IVA = total, con los precios que ya incluyen IVA. */
public class DesgloseIvaTest {

    private static final BigDecimal DIECINUEVE = new BigDecimal("0.19");

    @Test
    void separaElIvaDeUnTotalQueYaLoIncluye() {
        DesgloseIva desglose = DesgloseIva.deTotalConIva(new BigDecimal("119000"), DIECINUEVE);

        assertEquals(0, new BigDecimal("100000").compareTo(desglose.base()));
        assertEquals(0, new BigDecimal("19000").compareTo(desglose.iva()));
        assertEquals("19", desglose.porcentaje());
    }

    @Test
    void conRedondeoLaSumaSiempreCuadraConElTotal() {
        BigDecimal total = new BigDecimal("30000");

        DesgloseIva desglose = DesgloseIva.deTotalConIva(total, DIECINUEVE);

        assertEquals(new BigDecimal("25210.08"), desglose.base());
        assertEquals(0, total.compareTo(desglose.base().add(desglose.iva())));
    }

    @Test
    void unaTasaFueraDeRangoSeRechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> DesgloseIva.deTotalConIva(BigDecimal.TEN, new BigDecimal("1.5")));
        assertThrows(IllegalArgumentException.class,
                () -> DesgloseIva.deTotalConIva(BigDecimal.TEN, new BigDecimal("-0.1")));
    }

    @Test
    void elNumeroDeFacturaEsConsecutivoConFormatoFijo() {
        // RF-08.2
        assertEquals("FV-000007", new NumeroFactura(7).valor());
    }
}
