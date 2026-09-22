package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.exception.ArticuloNoEncontradoException;
import com.example.ecommerce.domain.repository.ArticuloRepository;

/** Detalle de un producto, usado tanto por el catálogo público como por el panel admin. */
public class ObtenerArticuloUseCase {

    private final ArticuloRepository repository;

    public ObtenerArticuloUseCase(ArticuloRepository repository) {
        this.repository = repository;
    }

    public Articulo ejecutar(long id) {
        return repository.obtenerPorId(id)
                .orElseThrow(ArticuloNoEncontradoException::new);
    }
}
