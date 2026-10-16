package com.example.ecommerce.infrastructure.security;

import com.example.ecommerce.application.TokenProvider;
import com.example.ecommerce.domain.entity.Sesion;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.TokenInvalidoException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Adaptador del puerto TokenProvider con JWT (jjwt). Es la única clase del
 * proyecto que conoce la librería JWT.
 *
 * El token lleva el id de la sesión (claim "jti") y el del usuario ("sub").
 * Su fecha de expiración es solo un tope absoluto de seguridad: la expiración
 * por inactividad (RF-01.6) y el cierre manual (RF-01.7) se controlan con la
 * sesión guardada en el servidor.
 */
public class JwtTokenProvider implements TokenProvider {

    private final SecretKey claveFirma;
    private final Duration duracionMaxima;
    private final Clock reloj;

    public JwtTokenProvider(String secreto, Duration duracionMaxima, Clock reloj) {
        this.claveFirma = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracionMaxima = duracionMaxima;
        this.reloj = reloj;
    }

    @Override
    public String generarToken(Usuario usuario, Sesion sesion) {
        Instant ahora = reloj.instant();

        // Patrón: Builder — Jwts.builder() arma el token paso a paso
        // (claims, fechas, firma) y al final produce el String inmutable.
        return Jwts.builder()
                .id(sesion.getId())
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracionMaxima)))
                .signWith(claveFirma)
                .compact();
    }

    @Override
    public DatosToken leer(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(claveFirma)
                    .clock(() -> Date.from(reloj.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new DatosToken(claims.getId(), Long.parseLong(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException ex) {
            throw new TokenInvalidoException();
        }
    }
}
