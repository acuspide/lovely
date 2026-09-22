package com.example.ecommerce.infrastructure.security;

import com.example.ecommerce.application.TokenProvider;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.TokenInvalidoException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Adaptador de infraestructura del puerto TokenProvider usando JWT (jjwt).
 * Es la única clase del proyecto que conoce la librería JWT; los casos de uso
 * solo dependen de la interfaz TokenProvider (DIP).
 */
public class JwtTokenProvider implements TokenProvider {

    private static final String CLAIM_ROL = "rol";
    private static final String CLAIM_ID = "id";

    private final SecretKey claveFirma;
    private final long minutosDeExpiracion;

    public JwtTokenProvider(String secreto, long minutosDeExpiracion) {
        this.claveFirma = Keys.hmacShaKeyFor(secreto.getBytes());
        this.minutosDeExpiracion = minutosDeExpiracion;
    }

    @Override
    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();

        // Patrón: Builder — Jwts.builder() arma el token paso a paso
        // (claims, fechas, firma) y al final produce el String inmutable.
        return Jwts.builder()
                .subject(usuario.getEmail().getValor())
                .claim(CLAIM_ID, usuario.getId())
                .claim(CLAIM_ROL, usuario.getRol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(minutosDeExpiracion, ChronoUnit.MINUTES)))
                .signWith(claveFirma)
                .compact();
    }

    @Override
    public long minutosDeExpiracion() {
        return minutosDeExpiracion;
    }

    @Override
    public DatosToken validar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(claveFirma)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            long id = claims.get(CLAIM_ID, Long.class);
            String rol = claims.get(CLAIM_ROL, String.class);
            return new DatosToken(id, claims.getSubject(), rol);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new TokenInvalidoException();
        }
    }
}
