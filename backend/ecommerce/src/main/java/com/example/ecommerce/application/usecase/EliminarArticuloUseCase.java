package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.exception.ArticuloConPedidosActivosException;
import com.example.ecommerce.domain.exception.ArticuloNoEncontradoException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.repository.PedidoRepository;

/**
 * CU-01, flujo "Eliminar Producto". Solo ADMINISTRADORA puede invocarlo
 * (restricción aplicada en la capa web con @PreAuthorize, no aquí).
 */
public class EliminarArticuloUseCase {

    private final ArticuloRepository articuloRepository;
    private final PedidoRepository pedidoRepository;

    public EliminarArticuloUseCase(ArticuloRepository articuloRepository, PedidoRepository pedidoRepository) {
        this.articuloRepository = articuloRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public void ejecutar(long id) {
        articuloRepository.obtenerPorId(id)
                .orElseThrow(ArticuloNoEncontradoException::new);

        // RN17: no se puede eliminar un producto con pedidos activos.
        if (pedidoRepository.existePedidoActivoConArticulo(id)) {
            throw new ArticuloConPedidosActivosException();
        }

        articuloRepository.eliminar(id);
    }
}
