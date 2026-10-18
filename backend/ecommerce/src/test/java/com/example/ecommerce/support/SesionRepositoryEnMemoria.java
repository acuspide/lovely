package com.example.ecommerce.support;

import com.example.ecommerce.domain.entity.Sesion;
import com.example.ecommerce.domain.repository.SesionRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Doble de pruebas del puerto SesionRepository; guarda y devuelve copias. */
public class SesionRepositoryEnMemoria implements SesionRepository {

    private final Map<String, Sesion> sesiones = new HashMap<>();

    @Override
    public Optional<Sesion> buscarPorId(String id) {
        return Optional.ofNullable(sesiones.get(id)).map(SesionRepositoryEnMemoria::copia);
    }

    @Override
    public Sesion guardar(Sesion sesion) {
        sesiones.put(sesion.getId(), copia(sesion));
        return copia(sesion);
    }

    @Override
    public void cerrarTodasDeUsuario(long usuarioId, Instant ahora) {
        sesiones.values().stream()
                .filter(sesion -> sesion.perteneceA(usuarioId))
                .forEach(sesion -> sesion.cerrar(ahora));
    }

    @Override
    public void cerrarOtrasDeUsuario(long usuarioId, String idSesionConservada, Instant ahora) {
        sesiones.values().stream()
                .filter(sesion -> sesion.perteneceA(usuarioId))
                .filter(sesion -> !sesion.getId().equals(idSesionConservada))
                .forEach(sesion -> sesion.cerrar(ahora));
    }

    public List<Sesion> abiertasDe(long usuarioId) {
        return sesiones.values().stream()
                .filter(sesion -> sesion.perteneceA(usuarioId))
                .filter(sesion -> sesion.getCerradaEn().isEmpty())
                .toList();
    }

    private static Sesion copia(Sesion sesion) {
        return Sesion.reconstruir(sesion.getId(), sesion.getUsuarioId(), sesion.getIniciadaEn(),
                sesion.getUltimaActividad(), sesion.getCerradaEn().orElse(null));
    }
}
