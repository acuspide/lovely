package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.exception.PedidoNoEncontradoException;
import com.example.ecommerce.domain.repository.PedidoRepository;

/**
 * RF-05.5: la clienta consulta un pedido suyo (por ejemplo, al recargar la
 * página de confirmación). Si el pedido es de otra persona responde igual que
 * si no existiera, para no revelar que existe.
 */
public class ObtenerPedidoDeClienteUseCase {

    private final PedidoRepository pedidoRepository;

    public ObtenerPedidoDeClienteUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Pedido ejecutar(long pedidoId, long clienteId) {
        return pedidoRepository.obtenerPorId(pedidoId)
                .filter(pedido -> pedido.getClienteId() == clienteId)
                .orElseThrow(PedidoNoEncontradoException::new);
    }
}
