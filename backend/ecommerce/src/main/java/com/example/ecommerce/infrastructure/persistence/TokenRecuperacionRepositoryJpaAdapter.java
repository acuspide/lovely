package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.TokenRecuperacion;
import com.example.ecommerce.domain.repository.TokenRecuperacionRepository;
import com.example.ecommerce.infrastructure.persistence.jpa.TokenRecuperacionJpaEntity;
import com.example.ecommerce.infrastructure.persistence.jpa.TokenRecuperacionJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Adaptador del puerto TokenRecuperacionRepository sobre Spring Data/Oracle.
 */
@Repository
public class TokenRecuperacionRepositoryJpaAdapter implements TokenRecuperacionRepository {

    private final TokenRecuperacionJpaRepository jpaRepository;

    public TokenRecuperacionRepositoryJpaAdapter(TokenRecuperacionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<TokenRecuperacion> buscarPorHuella(String huella) {
        return jpaRepository.findByHuella(huella).map(TokenRecuperacionRepositoryJpaAdapter::aDominio);
    }

    @Override
    public TokenRecuperacion guardar(TokenRecuperacion token) {
        return aDominio(jpaRepository.save(aEntidad(token)));
    }

    @Override
    @Transactional
    public void anularPendientesDe(long usuarioId, Instant ahora) {
        jpaRepository.anularPendientesDeUsuario(usuarioId, FechasUtc.aColumna(ahora));
    }

    private static TokenRecuperacion aDominio(TokenRecuperacionJpaEntity entidad) {
        return TokenRecuperacion.reconstruir(
                entidad.getId(),
                entidad.getUsuarioId(),
                entidad.getHuella(),
                FechasUtc.aInstante(entidad.getCreadoEn()),
                FechasUtc.aInstante(entidad.getExpiraEn()),
                FechasUtc.aInstante(entidad.getUsadoEn()));
    }

    private static TokenRecuperacionJpaEntity aEntidad(TokenRecuperacion token) {
        Long id = token.getId() == 0 ? null : token.getId();
        return new TokenRecuperacionJpaEntity(
                id,
                token.getUsuarioId(),
                token.getHuella(),
                FechasUtc.aColumna(token.getCreadoEn()),
                FechasUtc.aColumna(token.getExpiraEn()),
                FechasUtc.aColumna(token.getUsadoEn().orElse(null)));
    }
}
