package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.repository.PedidoRepository;

import java.util.List;

/** RF-07.8: "Mis pedidos" — la clienta ve sus pedidos y el estado de cada uno. */
public class ListarPedidosDeClienteUseCase {

    private final PedidoRepository pedidoRepository;

    public ListarPedidosDeClienteUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public List<Pedido> ejecutar(long clienteId) {
        return pedidoRepository.listarPorCliente(clienteId);
    }
}
