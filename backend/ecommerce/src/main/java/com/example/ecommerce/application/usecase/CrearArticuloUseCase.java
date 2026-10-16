package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.exception.NombreArticuloDuplicadoException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.NombreArticulo;
import com.example.ecommerce.domain.valueobject.Precio;

/**
 * RF-02.1 / CU-01 "Crear Producto": la Administradora registra un producto
 * nuevo con su stock inicial.
 *
 * <ul>
 *   <li>RF-02.2: las validaciones de cada dato las hacen el value object
 *       {@link NombreArticulo}, {@link Precio} y la entidad {@link Articulo}.</li>
 *   <li>RF-02.3: este caso de uso comprueba, con el repositorio, que el nombre
 *       no lo use otro producto.</li>
 * </ul>
 */
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

        // RF-02.3 / CU-01 A2: nombre duplicado (sin distinguir mayúsculas ni tildes).
        repository.buscarPorNombre(nombreArticulo).ifPresent(existente -> {
            throw new NombreArticuloDuplicadoException(existente.getNombre().getValor());
        });

        // id = 0: "sin persistir", igual que las cuentas de usuario (F-01).
        Articulo articuloSinPersistir = new Articulo(
                0L, nombreArticulo, descripcion, precio, categoria, marca, imagenUrl, stock);

        return repository.guardar(articuloSinPersistir);
    }
}
