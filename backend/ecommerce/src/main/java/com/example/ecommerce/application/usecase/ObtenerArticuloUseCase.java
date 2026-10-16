package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.exception.ArticuloNoEncontradoException;
import com.example.ecommerce.domain.repository.ArticuloRepository;

/**
 * Detalle de un producto (RF-02.5).
 *
 * <ul>
 *   <li>{@link #ejecutar}: catálogo público. Un producto desactivado
 *       (RF-02.7) se trata como si no existiera.</li>
 *   <li>{@link #ejecutarParaGestion}: panel de la Administradora; devuelve
 *       el producto esté activo o no.</li>
 * </ul>
 */
public class ObtenerArticuloUseCase {

    private final ArticuloRepository repository;

    public ObtenerArticuloUseCase(ArticuloRepository repository) {
        this.repository = repository;
    }

    public Articulo ejecutar(long id) {
        return repository.obtenerPorId(id)
                .filter(Articulo::isActivo)
                .orElseThrow(ArticuloNoEncontradoException::new);
    }

    public Articulo ejecutarParaGestion(long id) {
        return repository.obtenerPorId(id)
                .orElseThrow(ArticuloNoEncontradoException::new);
    }
}
