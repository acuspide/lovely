package com.example.ecommerce.infrastructure.persistence;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * El dominio trabaja con {@link Instant} y las columnas son TIMESTAMP sin zona,
 * así que la base de datos siempre guarda la hora en UTC. Centralizar la
 * conversión evita que algún mapper use por error la zona del servidor.
 */
final class FechasUtc {

    private FechasUtc() {
    }

    static LocalDateTime aColumna(Instant instante) {
        return instante == null ? null : LocalDateTime.ofInstant(instante, ZoneOffset.UTC);
    }

    static Instant aInstante(LocalDateTime columna) {
        return columna == null ? null : columna.toInstant(ZoneOffset.UTC);
    }
}
