package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.entity.Carrito;
import com.example.ecommerce.domain.exception.ArticuloNoRegistradoException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.repository.CarritoRepository;

/**
 * Agregar productos al carrito (base de F-04). Aplica las reglas de
 * disponibilidad de RF-02.7 (producto inactivo) y RF-03.4 (producto agotado).
 */
public class AgregarAlCarritoUseCase {

    private final CarritoRepository carritoRepository;
    private final ArticuloRepository articuloRepository;

    public AgregarAlCarritoUseCase(CarritoRepository carritoRepository, ArticuloRepository articuloRepository) {
        this.carritoRepository = carritoRepository;
        this.articuloRepository = articuloRepository;
    }

    /**
     * Agrega un producto al carrito de la clienta.
     *
     * <p>El precio y el stock se toman del producto guardado, nunca de lo que
     * envíe el navegador: así nadie puede comprar a otro precio ni saltarse
     * el control de existencias.</p>
     */
    public Carrito ejecutar(long carritoId, long clienteId, long articuloId, int cantidad) {

        Articulo articulo = articuloRepository.obtenerPorId(articuloId)
                .orElseThrow(ArticuloNoRegistradoException::new);

        // RF-02.7 y RF-03.4: ni inactivo ni agotado.
        articulo.verificarDisponibleParaVenta();

        Carrito carrito = carritoRepository.obtenerPorId(carritoId)
                .orElseGet(() -> new Carrito(carritoId, clienteId));

        carrito.agregarItem(articuloId, cantidad, articulo.getPrecio(), articulo.getStock());

        carritoRepository.guardar(carrito);

        return carrito;
    }
}
