package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.NombreArticuloRequeridoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** RF-02.3: reglas del nombre de producto y de su clave de comparación. */
class NombreArticuloTest {

    @Test
    void conservaMayusculasYTildesPeroQuitaEspaciosSobrantes() {
        // Act
        NombreArticulo nombre = new NombreArticulo("   Crema   Hidratante  Día ");

        // Assert
        assertEquals("Crema Hidratante Día", nombre.getValor());
    }

    @Test
    void laClaveIgnoraMayusculasTildesYEspacios() {
        // Act
        NombreArticulo nombre = new NombreArticulo("  Ñandú   CRÈME  ");

        // Assert
        assertEquals("nandu creme", nombre.clave());
    }

    @ParameterizedTest
    @ValueSource(strings = {"crema hidratante", "CREMA HIDRATANTE", "Crema  Hidratánte", " crema hidratante "})
    void nombresQueSoloCambianEnLaFormaDeEscribirseSonIguales(String variante) {
        // Assert
        assertEquals(new NombreArticulo("Crema Hidratante"), new NombreArticulo(variante));
    }

    @Test
    void nombresDistintosNoSonIguales() {
        // Assert
        assertNotEquals(new NombreArticulo("Crema Hidratante"), new NombreArticulo("Crema Nutritiva"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rechazaNombresVacios(String valor) {
        // Act y Assert
        assertThrows(NombreArticuloRequeridoException.class, () -> new NombreArticulo(valor));
    }

    @Test
    void normalizarSirveParaCompararBusquedasConNombres() {
        // RF-02.5: la búsqueda usa la misma normalización que la clave.
        assertEquals("crema hidratante", NombreArticulo.normalizar("  CREMA   Hidratánte "));
    }
}
