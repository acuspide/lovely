package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Carrito;
import com.example.ecommerce.domain.exception.ArticuloNoRegistradoException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.repository.CarritoRepository;
import com.example.ecommerce.domain.valueobject.Precio;

public class AgregarAlCarritoUseCase {

    private final CarritoRepository carritoRepository;
    private final ArticuloRepository articuloRepository;

    public AgregarAlCarritoUseCase(CarritoRepository carritoRepository, ArticuloRepository articuloRepository) {
        this.carritoRepository = carritoRepository;
        this.articuloRepository = articuloRepository;
    }

    // NOTA: precioUnitario y stockDisponible se reciben como parametros porque
    // Articulo/Inventario todavia no exponen un getter/metodo publico para leerlos.
    // Cuando Inventario tenga tieneDisponibilidad(int), este caso de uso debe
    // resolver ambos valores directamente desde el Articulo obtenido del repositorio.
    public Carrito ejecutar(
            long carritoId,
            long clienteId,
            long articuloId,
            int cantidad,
            Precio precioUnitario,
            int stockDisponible) {

        articuloRepository.obtenerPorId(articuloId)
                .orElseThrow(ArticuloNoRegistradoException::new);

        Carrito carrito = carritoRepository.obtenerPorId(carritoId)
                .orElseGet(() -> new Carrito(carritoId, clienteId));

        carrito.agregarItem(articuloId, cantidad, precioUnitario, stockDisponible);

        carritoRepository.guardar(carrito);

        return carrito;
    }
}
