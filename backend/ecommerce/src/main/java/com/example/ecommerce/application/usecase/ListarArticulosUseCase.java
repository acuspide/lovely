package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.NombreArticulo;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Caso de uso de solo lectura para el listado de productos, con búsqueda por
 * nombre y filtro por categoría (RF-02.5, RF-03.2 y RF-03.3), y orden
 * configurable en el catálogo público (RF-03.6).
 *
 * <ul>
 *   <li>{@link #ejecutar}: catálogo de las clientas; solo productos activos
 *       (RF-02.7).</li>
 *   <li>{@link #ejecutarParaGestion}: panel de la Administradora; incluye los
 *       productos desactivados para poder reactivarlos.</li>
 * </ul>
 *
 * Filtra en memoria sobre listarTodos(): el catálogo de una boutique es
 * pequeño, así que no vale la pena ampliar el puerto del repositorio con
 * múltiples métodos de consulta (ISP).
 */
public class ListarArticulosUseCase {

    private final ArticuloRepository repository;

    public ListarArticulosUseCase(ArticuloRepository repository) {
        this.repository = repository;
    }

    /**
     * RF-03.4: en el catálogo de las clientas los agotados van al final, para
     * que lo primero que vean sea lo que sí pueden comprar.
     */
    private static final Comparator<Articulo> AGOTADOS_AL_FINAL = Comparator.comparing(Articulo::estaAgotado);

    /** Catálogo público ordenado por nombre: solo productos activos. */
    public List<Articulo> ejecutar(Optional<CategoriaArticulo> categoria, Optional<String> busqueda) {
        return ejecutar(categoria, busqueda, OrdenCatalogo.NOMBRE);
    }

    /**
     * Catálogo público (RF-03): solo productos activos, con los agotados al
     * final y el resto en el orden que elija la clienta (RF-03.6).
     */
    public List<Articulo> ejecutar(Optional<CategoriaArticulo> categoria, Optional<String> busqueda,
                                   OrdenCatalogo orden) {
        return listar(categoria, busqueda, Articulo::isActivo, AGOTADOS_AL_FINAL.thenComparing(orden.comparador()));
    }

    /** Panel de la Administradora: productos activos e inactivos, por nombre. */
    public List<Articulo> ejecutarParaGestion(Optional<CategoriaArticulo> categoria, Optional<String> busqueda) {
        return listar(categoria, busqueda, articulo -> true, OrdenCatalogo.NOMBRE.comparador());
    }

    private List<Articulo> listar(Optional<CategoriaArticulo> categoria,
                                  Optional<String> busqueda,
                                  Predicate<Articulo> visible,
                                  Comparator<Articulo> orden) {
        return repository.listarTodos().stream()
                .filter(visible)
                .filter(articulo -> categoria.isEmpty() || articulo.getCategoria() == categoria.get())
                .filter(articulo -> busqueda.isEmpty() || busqueda.get().isBlank()
                        || coincideNombre(articulo, busqueda.get()))
                .sorted(orden)
                .toList();
    }

    /**
     * RF-02.5: búsqueda parcial que ignora mayúsculas, tildes y espacios de
     * más ("crema" encuentra "Crema Hidratánte"). Una búsqueda en blanco no filtra.
     */
    private boolean coincideNombre(Articulo articulo, String busqueda) {
        return articulo.getNombre().clave().contains(NombreArticulo.normalizar(busqueda));
    }
}
