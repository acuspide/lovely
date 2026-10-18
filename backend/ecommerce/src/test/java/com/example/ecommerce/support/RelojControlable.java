package com.example.ecommerce.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Reloj de pruebas que solo avanza cuando la prueba lo pide. Permite verificar
 * reglas de tiempo (bloqueo de 15 minutos, sesión de 30 minutos, enlace de 30
 * minutos) sin esperar de verdad.
 */
public class RelojControlable extends Clock {

    private Instant ahora;

    public RelojControlable(Instant inicio) {
        this.ahora = inicio;
    }

    public void avanzar(Duration duracion) {
        ahora = ahora.plus(duracion);
    }

    @Override
    public Instant instant() {
        return ahora;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zona) {
        return this;
    }
}
