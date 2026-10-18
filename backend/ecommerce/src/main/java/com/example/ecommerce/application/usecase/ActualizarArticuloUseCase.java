package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.exception.ArticuloNoEncontradoException;
import com.example.ecommerce.domain.exception.NombreArticuloDuplicadoException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.NombreArticulo;
import com.example.ecommerce.domain.valueobject.Precio;

/**
 * RF-02.6 / CU-01 "Editar Producto": la Administradora modifica los datos de
 * un producto existente con las mismas validaciones del registro (RF-02.2) y
 * sin repetir nombre (RF-02.3).
 *
 * <p>El stock no se edita aquí: solo cambia por el módulo de inventario
 * (entradas de mercancía y ventas), para no perder la trazabilidad.</p>
 */
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
            String imagenUrl) {

        Articulo articulo = repository.obtenerPorId(id)
                .orElseThrow(ArticuloNoEncontradoException::new);

        NombreArticulo nombreArticulo = new NombreArticulo(nombre);

        // RF-02.3: el nombre puede no haber cambiado (o cambiar solo en mayúsculas
        // o tildes); solo es conflicto si pertenece a OTRO producto.
        repository.buscarPorNombre(nombreArticulo)
                .filter(otro -> otro.getId() != id)
                .ifPresent(otro -> {
                    throw new NombreArticuloDuplicadoException(otro.getNombre().getValor());
                });

        articulo.actualizar(nombreArticulo, descripcion, precio, categoria, marca, imagenUrl);

        return repository.guardar(articulo);
    }
}
