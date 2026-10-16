package com.example.ecommerce.domain.repository;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.valueobject.NombreArticulo;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (arquitectura hexagonal) para persistir productos del
 * catálogo. El dominio solo conoce esta interfaz; los adaptadores
 * (Oracle/JPA en producción, memoria en pruebas) la implementan.
 */
public interface ArticuloRepository {

    Optional<Articulo> obtenerPorId(long id);

    /**
     * F-06 / RF-06.5: como {@link #obtenerPorId(long)}, pero bloquea el
     * producto hasta que termine la transacción en curso. Así, si dos clientas
     * compran a la vez las últimas unidades, la segunda espera y ve el stock
     * ya descontado (no hay sobreventa).
     */
    Optional<Articulo> obtenerParaActualizar(long id);

    /**
     * RF-02.3: busca un producto cuyo nombre sea "el mismo" que el indicado,
     * comparando por {@link NombreArticulo#clave()} (sin distinguir
     * mayúsculas, tildes ni espacios repetidos).
     */
    Optional<Articulo> buscarPorNombre(NombreArticulo nombre);

    List<Articulo> listarTodos();

    /**
     * Persiste el artículo y devuelve la entidad resultante (con id asignado
     * si llegó "sin persistir", ver {@link Articulo#conId(long)}), mismo
     * contrato que {@code UsuarioRepository.guardar}.
     */
    Articulo guardar(Articulo articulo);

    void eliminar(long id);
}
