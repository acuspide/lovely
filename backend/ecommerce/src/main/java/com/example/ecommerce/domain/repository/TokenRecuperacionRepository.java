package com.example.ecommerce.domain.repository;

import com.example.ecommerce.domain.entity.TokenRecuperacion;

import java.time.Instant;
import java.util.Optional;

public interface TokenRecuperacionRepository {
    Optional<TokenRecuperacion> buscarPorHuella(String huella);
    TokenRecuperacion guardar(TokenRecuperacion token);

    /** Invalida los enlaces pendientes del usuario: solo el último que pidió debe funcionar. */
    void anularPendientesDe(long usuarioId, Instant ahora);
}
