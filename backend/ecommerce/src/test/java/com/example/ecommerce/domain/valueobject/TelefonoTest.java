package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.TelefonoInvalidoException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TelefonoTest {

    @ParameterizedTest
    @ValueSource(strings = {"3001234567", "300 123 4567", "300-123-4567", "+57 300 123 4567", "573001234567"})
    void normalizaLasFormasHabitualesDeEscribirUnCelular(String escrito) {
        // Act
        Telefono telefono = new Telefono(escrito);

        // Assert
        assertEquals("3001234567", telefono.getValor());
    }

    @ParameterizedTest
    @ValueSource(strings = {"6067451234", "(606) 745 1234"})
    void aceptaTelefonosFijosDeDiezDigitos(String escrito) {
        // Act y Assert
        assertEquals("6067451234", new Telefono(escrito).getValor());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"300123456", "30012345678", "1234567890", "300abc4567"})
    void rechazaTelefonosInvalidos(String escrito) {
        // Act y Assert
        assertThrows(TelefonoInvalidoException.class, () -> new Telefono(escrito));
    }
}
