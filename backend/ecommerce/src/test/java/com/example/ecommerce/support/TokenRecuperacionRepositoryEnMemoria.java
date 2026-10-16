package com.example.ecommerce.support;

import com.example.ecommerce.domain.entity.TokenRecuperacion;
import com.example.ecommerce.domain.repository.TokenRecuperacionRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** Doble de pruebas del puerto TokenRecuperacionRepository; guarda y devuelve copias. */
public class TokenRecuperacionRepositoryEnMemoria implements TokenRecuperacionRepository {

    private final Map<Long, TokenRecuperacion> tokens = new HashMap<>();
    private final AtomicLong secuenciaId = new AtomicLong(0);

    @Override
    public Optional<TokenRecuperacion> buscarPorHuella(String huella) {
        return tokens.values().stream()
                .filter(token -> token.getHuella().equals(huella))
                .findFirst()
                .map(TokenRecuperacionRepositoryEnMemoria::copia);
    }

    @Override
    public TokenRecuperacion guardar(TokenRecuperacion token) {
        long id = token.getId() == 0 ? secuenciaId.incrementAndGet() : token.getId();
        TokenRecuperacion guardado = token.conId(id);
        tokens.put(id, guardado);
        return copia(guardado);
    }

    @Override
    public void anularPendientesDe(long usuarioId, Instant ahora) {
        tokens.values().stream()
                .filter(token -> token.getUsuarioId() == usuarioId)
                .filter(token -> token.esUsable(ahora))
                .forEach(token -> token.marcarUsado(ahora));
    }

    private static TokenRecuperacion copia(TokenRecuperacion token) {
        return token.conId(token.getId());
    }
}
