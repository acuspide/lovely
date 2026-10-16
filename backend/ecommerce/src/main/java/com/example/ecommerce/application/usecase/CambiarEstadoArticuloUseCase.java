package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.exception.ArticuloNoEncontradoException;
import com.example.ecommerce.domain.repository.ArticuloRepository;

/**
 * RF-02.7: la Administradora desactiva o reactiva un producto del catálogo.
 *
 * <p>Un producto desactivado no se borra: deja de aparecer en el catálogo de
 * las clientas y no se puede agregar al carrito, pero conserva sus datos y
 * su historial de pedidos. Es la salida que sugiere el sistema cuando un
 * producto no se puede eliminar por tener pedidos activos (RF-02.9).</p>
 *
 * <p>Mismo diseño que {@code CambiarEstadoUsuarioUseCase} en F-01. Solo
 * ADMINISTRADORA puede invocarlo (restricción aplicada en la capa web).</p>
 */
public class CambiarEstadoArticuloUseCase {

    private final ArticuloRepository repository;

    public CambiarEstadoArticuloUseCase(ArticuloRepository repository) {
        this.repository = repository;
    }

    public Articulo ejecutar(long id, boolean activo) {
        Articulo articulo = repository.obtenerPorId(id)
                .orElseThrow(ArticuloNoEncontradoException::new);

        if (activo) {
            articulo.reactivar();
        } else {
            articulo.desactivar();
        }

        return repository.guardar(articulo);
    }
}
