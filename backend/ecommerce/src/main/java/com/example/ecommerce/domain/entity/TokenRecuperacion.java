package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.EnlaceRecuperacionInvalidoException;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * RF-01.8: enlace de recuperación de contraseña, de un solo uso y válido
 * durante 30 minutos.
 *
 * Solo se guarda la huella (hash) del token, nunca el token en sí: quien lea
 * la base de datos no puede reconstruir un enlace válido.
 */
public class TokenRecuperacion {

    public static final Duration VIGENCIA = Duration.ofMinutes(30);

    private final long id;
    private final long usuarioId;
    private final String huella;
    private final Instant creadoEn;
    private final Instant expiraEn;
    private Instant usadoEn;

    private TokenRecuperacion(long id, long usuarioId, String huella, Instant creadoEn,
                              Instant expiraEn, Instant usadoEn) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.huella = Objects.requireNonNull(huella, "El token de recuperación debe tener huella.");
        this.creadoEn = Objects.requireNonNull(creadoEn);
        this.expiraEn = Objects.requireNonNull(expiraEn);
        this.usadoEn = usadoEn;
    }

    public static TokenRecuperacion emitir(long usuarioId, String huella, Instant ahora) {
        return new TokenRecuperacion(0L, usuarioId, huella, ahora, ahora.plus(VIGENCIA), null);
    }

    public static TokenRecuperacion reconstruir(long id, long usuarioId, String huella, Instant creadoEn,
                                                Instant expiraEn, Instant usadoEn) {
        return new TokenRecuperacion(id, usuarioId, huella, creadoEn, expiraEn, usadoEn);
    }

    public TokenRecuperacion conId(long nuevoId) {
        return new TokenRecuperacion(nuevoId, usuarioId, huella, creadoEn, expiraEn, usadoEn);
    }

    public boolean esUsable(Instant ahora) {
        return usadoEn == null && ahora.isBefore(expiraEn);
    }

    /** Consume el enlace: a partir de aquí ya no sirve para nada más. */
    public void marcarUsado(Instant ahora) {
        if (!esUsable(ahora)) {
            throw new EnlaceRecuperacionInvalidoException();
        }
        this.usadoEn = ahora;
    }

    public long getId() { return id; }
    public long getUsuarioId() { return usuarioId; }
    public String getHuella() { return huella; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getExpiraEn() { return expiraEn; }
    public Optional<Instant> getUsadoEn() { return Optional.ofNullable(usadoEn); }
}
