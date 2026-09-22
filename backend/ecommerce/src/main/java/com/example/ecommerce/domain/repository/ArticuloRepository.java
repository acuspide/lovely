package com.example.ecommerce.domain.repository;

import com.example.ecommerce.domain.entity.Articulo;

import java.util.List;
import java.util.Optional;

public interface ArticuloRepository {
    Optional<Articulo> obtenerPorId(long id);
    Optional<Articulo> buscarPorNombre(String nombre);
    List<Articulo> listarTodos();

    /**
     * Persiste el artículo y devuelve la entidad resultante (con id asignado
     * si llegó "sin persistir", ver {@link Articulo#conId(long)}), mismo
     * contrato que {@code UsuarioRepository.guardar}.
     */
    Articulo guardar(Articulo articulo);

    void eliminar(long id);
}
