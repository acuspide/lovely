package com.example.ecommerce.domain.entity;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Sesión iniciada por un usuario. Existe del lado del servidor para que el
 * acceso se pueda anular de verdad:
 *  - RF-01.6: vence tras un periodo sin actividad; cada petición autenticada
 *    la renueva.
 *  - RF-01.7: al cerrar sesión queda cerrada, así que el token que la
 *    representa deja de servir aunque alguien lo conserve.
 */
public class Sesion {

    private final String id;
    private final long usuarioId;
    private final Instant iniciadaEn;
    private Instant ultimaActividad;
    private Instant cerradaEn;

    private Sesion(String id, long usuarioId, Instant iniciadaEn, Instant ultimaActividad, Instant cerradaEn) {
        this.id = Objects.requireNonNull(id, "La sesión debe tener un identificador.");
        this.usuarioId = usuarioId;
        this.iniciadaEn = Objects.requireNonNull(iniciadaEn);
        this.ultimaActividad = Objects.requireNonNull(ultimaActividad);
        this.cerradaEn = cerradaEn;
    }

    public static Sesion iniciar(String id, long usuarioId, Instant ahora) {
        return new Sesion(id, usuarioId, ahora, ahora, null);
    }

    public static Sesion reconstruir(String id, long usuarioId, Instant iniciadaEn,
                                     Instant ultimaActividad, Instant cerradaEn) {
        return new Sesion(id, usuarioId, iniciadaEn, ultimaActividad, cerradaEn);
    }

    public boolean estaVigente(Instant ahora, Duration inactividadMaxima) {
        return cerradaEn == null && ahora.isBefore(expiraEn(inactividadMaxima));
    }

    public Instant expiraEn(Duration inactividadMaxima) {
        return ultimaActividad.plus(inactividadMaxima);
    }

    public void registrarActividad(Instant ahora) {
        if (ahora.isAfter(ultimaActividad)) {
            this.ultimaActividad = ahora;
        }
    }

    public void cerrar(Instant ahora) {
        if (cerradaEn == null) {
            this.cerradaEn = ahora;
        }
    }

    public boolean perteneceA(long idUsuario) {
        return usuarioId == idUsuario;
    }

    public String getId() { return id; }
    public long getUsuarioId() { return usuarioId; }
    public Instant getIniciadaEn() { return iniciadaEn; }
    public Instant getUltimaActividad() { return ultimaActividad; }
    public Optional<Instant> getCerradaEn() { return Optional.ofNullable(cerradaEn); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sesion otra)) return false;
        return id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
