package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Caso de uso de solo lectura para el catálogo (público, RF-03) y el panel
 * admin (RF-02). Filtra en memoria sobre listarTodos(): el catálogo de una
 * boutique es pequeño, así que no vale la pena ampliar el puerto del
 * repositorio con múltiples métodos de consulta (ISP).
 */
public class ListarArticulosUseCase {

    private final ArticuloRepository repository;

    public ListarArticulosUseCase(ArticuloRepository repository) {
        this.repository = repository;
    }

    public List<Articulo> ejecutar(Optional<CategoriaArticulo> categoria, Optional<String> busqueda) {
        return repository.listarTodos().stream()
                .filter(articulo -> categoria.isEmpty() || articulo.getCategoria() == categoria.get())
                .filter(articulo -> busqueda.isEmpty() || coincideNombre(articulo, busqueda.get()))
                .toList();
    }

    private boolean coincideNombre(Articulo articulo, String busqueda) {
        return articulo.getNombre().getValor()
                .toLowerCase(Locale.ROOT)
                .contains(busqueda.toLowerCase(Locale.ROOT));
    }
}
