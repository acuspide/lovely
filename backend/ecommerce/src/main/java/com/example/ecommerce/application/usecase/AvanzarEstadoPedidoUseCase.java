package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.GestorTransacciones;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.exception.PedidoNoEncontradoException;
import com.example.ecommerce.domain.repository.PedidoRepository;
import com.example.ecommerce.domain.valueobject.EstadoPedido;

import java.time.Clock;

/**
 * RF-07.3 / RF-07.4: el personal (Administradora o Asesora de ventas) pasa
 * un pedido al siguiente estado del diagrama: Confirmado → En proceso → En
 * camino → Entregado → Completado. Nunca hacia atrás.
 *
 * <p>Quien hace el cambio envía también el estado al que quiere llevarlo. Si
 * otra persona ya lo cambió (por ejemplo, la clienta lo canceló), el paso ya
 * no es el siguiente y se rechaza, en lugar de avanzar a ciegas.</p>
 */
public class AvanzarEstadoPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final GestorTransacciones transacciones;
    private final Clock reloj;

    public AvanzarEstadoPedidoUseCase(PedidoRepository pedidoRepository, GestorTransacciones transacciones,
                                      Clock reloj) {
        this.pedidoRepository = pedidoRepository;
        this.transacciones = transacciones;
        this.reloj = reloj;
    }

    /**
     * @param usuarioId cuenta del personal que hace el cambio (queda en el historial)
     * @throws PedidoNoEncontradoException si el pedido no existe
     * @throws com.example.ecommerce.domain.exception.TransicionEstadoInvalidaException
     *         si {@code nuevoEstado} no es el siguiente del diagrama
     */
    public Pedido ejecutar(long pedidoId, EstadoPedido nuevoEstado, long usuarioId) {
        return transacciones.enTransaccion(() -> {
            Pedido pedido = pedidoRepository.obtenerParaActualizar(pedidoId)
                    .orElseThrow(PedidoNoEncontradoException::new);
            pedido.avanzarA(nuevoEstado, usuarioId, reloj.instant());
            return pedidoRepository.guardar(pedido);
        });
    }
}
