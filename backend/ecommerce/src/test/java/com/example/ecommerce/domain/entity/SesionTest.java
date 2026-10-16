package com.example.ecommerce.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SesionTest {

    private static final Instant INICIO = Instant.parse("2026-10-14T15:00:00Z");
    private static final Duration TREINTA_MINUTOS = Duration.ofMinutes(30);

    @Test
    void unaSesionNuevaVenceTreintaMinutosDespuesDeIniciar() {
        // Act
        Sesion sesion = Sesion.iniciar("s-1", 1L, INICIO);

        // Assert
        assertEquals(INICIO.plus(TREINTA_MINUTOS), sesion.expiraEn(TREINTA_MINUTOS));
        assertTrue(sesion.estaVigente(INICIO.plus(Duration.ofMinutes(29)), TREINTA_MINUTOS));
        assertFalse(sesion.estaVigente(INICIO.plus(TREINTA_MINUTOS), TREINTA_MINUTOS));
    }

    @Test
    void laActividadRenuevaElVencimiento() {
        // Arrange
        Sesion sesion = Sesion.iniciar("s-1", 1L, INICIO);
        Instant actividad = INICIO.plus(Duration.ofMinutes(20));

        // Act
        sesion.registrarActividad(actividad);

        // Assert
        assertTrue(sesion.estaVigente(INICIO.plus(Duration.ofMinutes(45)), TREINTA_MINUTOS));
        assertEquals(actividad.plus(TREINTA_MINUTOS), sesion.expiraEn(TREINTA_MINUTOS));
    }

    @Test
    void unaSesionCerradaDejaDeEstarVigenteDeInmediato() {
        // Arrange
        Sesion sesion = Sesion.iniciar("s-1", 1L, INICIO);

        // Act
        sesion.cerrar(INICIO.plusSeconds(5));

        // Assert
        assertFalse(sesion.estaVigente(INICIO.plusSeconds(6), TREINTA_MINUTOS));
    }
}
