package com.example.ecommerce.domain.service;

import com.example.ecommerce.domain.exception.ContrasenaDebilException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PoliticaContrasenaTest {

    @ParameterizedTest
    @ValueSource(strings = {"Lovely2026*", "abcdef1!", "ñandú123#", "1234567a.", "Clave-2026_con20car"})
    void aceptaContrasenasConLetraNumeroYCaracterEspecial(String contrasena) {
        // Act y Assert
        assertDoesNotThrow(() -> PoliticaContrasena.validar(contrasena));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "abc12!",       // menos de 8 caracteres
            "abcdefgh!",    // sin número
            "12345678!",    // sin letra
            "Lovely2026",   // sin carácter especial
            "Lovely 2026",  // el espacio no cuenta como carácter especial
            "        "
    })
    void rechazaContrasenasDebiles(String contrasena) {
        // Act y Assert
        assertThrows(ContrasenaDebilException.class, () -> PoliticaContrasena.validar(contrasena));
    }

    @Test
    void rechazaContrasenasDeMasDeVeinteCaracteres() {
        // Arrange: 21 caracteres
        String larga = "Lovely2026*Lovely2026";

        // Act y Assert
        assertThrows(ContrasenaDebilException.class, () -> PoliticaContrasena.validar(larga));
    }

    @Test
    void aceptaExactamenteVeinteCaracteres() {
        // Arrange: 20 caracteres
        String limite = "Lovely2026*Lovely202";

        // Act y Assert
        assertDoesNotThrow(() -> PoliticaContrasena.validar(limite));
    }
}
