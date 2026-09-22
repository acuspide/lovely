package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.exception.NombreArticuloDuplicadoException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.NombreArticulo;
import com.example.ecommerce.domain.valueobject.Precio;

public class CrearArticuloUseCase {

    private final ArticuloRepository repository;

    public CrearArticuloUseCase(ArticuloRepository repository) {
        this.repository = repository;
    }

    public Articulo ejecutar(
            String nombre,
            String descripcion,
            Precio precio,
            CategoriaArticulo categoria,
            String marca,
            String imagenUrl,
            int stock) {

        NombreArticulo nombreArticulo = new NombreArticulo(nombre);

        // CU-01, curso alterno A2: nombre de producto duplicado.
        if (repository.buscarPorNombre(nombre).isPresent()) {
            throw new NombreArticuloDuplicadoException();
        }

        // id = 0: "sin persistir", igual que RegistrarUsuarioUseCase (F-01).
        Articulo articuloSinPersistir = new Articulo(
                0L, nombreArticulo, descripcion, precio, categoria, marca, imagenUrl, stock);

        return repository.guardar(articuloSinPersistir);
    }
}
