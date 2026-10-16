package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.EnlaceRecuperacionInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenRecuperacionTest {

    private static final Instant EMISION = Instant.parse("2026-10-14T15:00:00Z");

    @Test
    void elEnlaceSirveDuranteTreintaMinutos() {
        // Act
        TokenRecuperacion token = TokenRecuperacion.emitir(1L, "huella", EMISION);

        // Assert
        assertTrue(token.esUsable(EMISION.plus(Duration.ofMinutes(29))));
        assertFalse(token.esUsable(EMISION.plus(Duration.ofMinutes(30))));
    }

    @Test
    void elEnlaceSoloSePuedeUsarUnaVez() {
        // Arrange
        TokenRecuperacion token = TokenRecuperacion.emitir(1L, "huella", EMISION);

        // Act
        assertDoesNotThrow(() -> token.marcarUsado(EMISION.plusSeconds(10)));

        // Assert
        assertThrows(EnlaceRecuperacionInvalidoException.class, () -> token.marcarUsado(EMISION.plusSeconds(20)));
    }

    @Test
    void unEnlaceVencidoNoSePuedeUsar() {
        // Arrange
        TokenRecuperacion token = TokenRecuperacion.emitir(1L, "huella", EMISION);

        // Act y Assert
        assertThrows(EnlaceRecuperacionInvalidoException.class,
                () -> token.marcarUsado(EMISION.plus(Duration.ofMinutes(31))));
    }
}
