package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.exception.ArticuloNoEncontradoException;
import com.example.ecommerce.domain.exception.NombreArticuloDuplicadoException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.NombreArticulo;
import com.example.ecommerce.domain.valueobject.Precio;

public class ActualizarArticuloUseCase {

    private final ArticuloRepository repository;

    public ActualizarArticuloUseCase(ArticuloRepository repository) {
        this.repository = repository;
    }

    public Articulo ejecutar(
            long id,
            String nombre,
            String descripcion,
            Precio precio,
            CategoriaArticulo categoria,
            String marca,
            String imagenUrl,
            int stock) {

        Articulo articulo = repository.obtenerPorId(id)
                .orElseThrow(ArticuloNoEncontradoException::new);

        NombreArticulo nombreArticulo = new NombreArticulo(nombre);

        // El nombre puede no haber cambiado: solo es conflicto si pertenece a OTRO producto.
        repository.buscarPorNombre(nombre)
                .filter(otro -> otro.getId() != id)
                .ifPresent(otro -> { throw new NombreArticuloDuplicadoException(); });

        articulo.actualizar(nombreArticulo, descripcion, precio, categoria, marca, imagenUrl, stock);

        return repository.guardar(articulo);
    }
}
