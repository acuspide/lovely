package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.exception.ArticuloConPedidosActivosException;
import com.example.ecommerce.domain.exception.ArticuloNoEncontradoException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.repository.PedidoRepository;

/**
 * RF-02.8 / CU-01, flujo "Eliminar Producto". Solo ADMINISTRADORA puede
 * invocarlo (restricción aplicada en la capa web con @PreAuthorize, no aquí).
 *
 * <p>RF-02.9: si el producto aparece en pedidos activos no se elimina, para no
 * dejar esos pedidos apuntando a un producto inexistente.</p>
 */
public class EliminarArticuloUseCase {

    private final ArticuloRepository articuloRepository;
    private final PedidoRepository pedidoRepository;

    public EliminarArticuloUseCase(ArticuloRepository articuloRepository, PedidoRepository pedidoRepository) {
        this.articuloRepository = articuloRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public void ejecutar(long id) {
        // RF-02.9 / RN17: no se puede eliminar un producto con pedidos activos;
        // la excepción sugiere desactivarlo (RF-02.7).
        if (!puedeEliminarse(id)) {
            throw new ArticuloConPedidosActivosException();
        }

        articuloRepository.eliminar(id);
    }

    /**
     * RF-02.9: consulta previa, sin borrar nada, para que la pantalla avise de
     * inmediato (antes de pedir confirmación) si el producto no se puede
     * eliminar. La regla es la misma que aplica {@link #ejecutar(long)}.
     */
    public boolean puedeEliminarse(long id) {
        articuloRepository.obtenerPorId(id)
                .orElseThrow(ArticuloNoEncontradoException::new);
        return !pedidoRepository.existePedidoActivoConArticulo(id);
    }
}
