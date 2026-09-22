package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PrecioTest {

    @Test
    void dosPreciosConElMismoValorDebenSerIguales() {

        // Arrange
        Precio precio1 = new Precio(new BigDecimal("15000"));
        Precio precio2 = new Precio(new BigDecimal("15000"));

        // Act
        // No necesitamos una acción adicional porque equals()
        // es la operación que queremos probar.

        // Assert
        assertEquals(precio1, precio2);
    }
    @Test
    void unPrecioNegativoDebeLanzarReglaDominioException() {

        // Arrange
        BigDecimal valorInvalido = new BigDecimal("-100");

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            new Precio(valorInvalido);
        });
    }

}
