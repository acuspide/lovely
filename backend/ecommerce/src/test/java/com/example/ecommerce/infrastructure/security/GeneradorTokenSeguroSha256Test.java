package com.example.ecommerce.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneradorTokenSeguroSha256Test {

    private final GeneradorTokenSeguroSha256 generador = new GeneradorTokenSeguroSha256();

    @Test
    void generaTokensDistintosYAptosParaUnaUrl() {
        // Act
        String uno = generador.generar();
        String otro = generador.generar();

        // Assert
        assertNotEquals(uno, otro);
        assertTrue(uno.matches("^[A-Za-z0-9_-]{43}$"), "Token inesperado: " + uno);
    }

    @Test
    void laHuellaEsDeterministaYDistintaDelToken() {
        // Arrange
        String token = generador.generar();

        // Act
        String huella = generador.calcularHuella(token);

        // Assert
        assertEquals(huella, generador.calcularHuella(token));
        assertNotEquals(token, huella);
        assertEquals(64, huella.length());
    }
}
