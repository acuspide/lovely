package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.valueobject.NombreArticulo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Doble de pruebas del puerto ArticuloRepository (tests unitarios de los
 * casos de uso). El adaptador de producción es ArticuloRepositoryJpaAdapter
 * (Oracle) — mismo patrón que UsuarioRepositoryEnMemoria en F-01.
 */
public class ArticuloRepositoryEnMemoria implements ArticuloRepository {

    private final Map<Long, Articulo> articulos = new HashMap<>();
    private final AtomicLong secuenciaId = new AtomicLong(0);

    @Override
    public Optional<Articulo> obtenerPorId(long id) {
        return Optional.ofNullable(articulos.get(id));
    }

    @Override
    public Optional<Articulo> buscarPorNombre(String nombre) {
        return articulos.values().stream()
                .filter(articulo -> articulo.getNombre().equals(new NombreArticulo(nombre)))
                .findFirst();
    }

    @Override
    public List<Articulo> listarTodos() {
        return List.copyOf(articulos.values());
    }

    @Override
    public Articulo guardar(Articulo articulo) {
        Articulo aGuardar = articulo.getId() == 0
                ? articulo.conId(secuenciaId.incrementAndGet())
                : articulo;
        articulos.put(aGuardar.getId(), aGuardar);
        return aGuardar;
    }

    @Override
    public void eliminar(long id) {
        articulos.remove(id);
    }
}
