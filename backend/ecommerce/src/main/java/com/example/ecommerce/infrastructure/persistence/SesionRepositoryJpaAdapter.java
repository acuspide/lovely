package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Sesion;
import com.example.ecommerce.domain.repository.SesionRepository;
import com.example.ecommerce.infrastructure.persistence.jpa.SesionJpaEntity;
import com.example.ecommerce.infrastructure.persistence.jpa.SesionJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Adaptador del puerto SesionRepository sobre Spring Data/Oracle.
 */
@Repository
public class SesionRepositoryJpaAdapter implements SesionRepository {

    private final SesionJpaRepository jpaRepository;

    public SesionRepositoryJpaAdapter(SesionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Sesion> buscarPorId(String id) {
        return jpaRepository.findById(id).map(SesionRepositoryJpaAdapter::aDominio);
    }

    @Override
    public Sesion guardar(Sesion sesion) {
        return aDominio(jpaRepository.save(aEntidad(sesion)));
    }

    @Override
    @Transactional
    public void cerrarTodasDeUsuario(long usuarioId, Instant ahora) {
        jpaRepository.cerrarAbiertasDeUsuario(usuarioId, FechasUtc.aColumna(ahora));
    }

    @Override
    @Transactional
    public void cerrarOtrasDeUsuario(long usuarioId, String idSesionConservada, Instant ahora) {
        jpaRepository.cerrarOtrasAbiertasDeUsuario(usuarioId, idSesionConservada, FechasUtc.aColumna(ahora));
    }

    private static Sesion aDominio(SesionJpaEntity entidad) {
        return Sesion.reconstruir(
                entidad.getId(),
                entidad.getUsuarioId(),
                FechasUtc.aInstante(entidad.getIniciadaEn()),
                FechasUtc.aInstante(entidad.getUltimaActividad()),
                FechasUtc.aInstante(entidad.getCerradaEn()));
    }

    private static SesionJpaEntity aEntidad(Sesion sesion) {
        return new SesionJpaEntity(
                sesion.getId(),
                sesion.getUsuarioId(),
                FechasUtc.aColumna(sesion.getIniciadaEn()),
                FechasUtc.aColumna(sesion.getUltimaActividad()),
                FechasUtc.aColumna(sesion.getCerradaEn().orElse(null)));
    }
}
