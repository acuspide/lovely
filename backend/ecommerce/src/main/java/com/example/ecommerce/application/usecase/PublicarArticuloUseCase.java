package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.*;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.valueobject.*;

import java.util.List;

public class PublicarArticuloUseCase {

    private final ArticuloRepository repository;

    public PublicarArticuloUseCase(ArticuloRepository repository) {
        this.repository = repository;
    }

    public Articulo ejecutar(
            long id,
            NombreArticulo nombre,
            Precio precio,
            Categoria categoria,
            Marca marca,
            Tono tono,
            List<TipoPiel> tiposPiel,
            Inventario inventario,
            FechaVencimiento fechaVencimiento,
            Tienda tienda) {

        Articulo articulo = new Articulo(
                id,
                nombre,
                precio,
                categoria,
                marca,
                tono,
                tiposPiel,
                inventario,
                fechaVencimiento,
                tienda
        );

        articulo.publicar();

        repository.guardar(articulo);

        return articulo;
    }
}
