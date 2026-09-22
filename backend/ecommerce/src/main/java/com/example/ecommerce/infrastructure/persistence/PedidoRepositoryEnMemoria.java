package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.repository.PedidoRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PedidoRepositoryEnMemoria implements PedidoRepository {

    private final Map<Long, Pedido> pedidos = new HashMap<>();

    @Override
    public Optional<Pedido> obtenerPorId(long id) {
        return Optional.ofNullable(pedidos.get(id));
    }

    @Override
    public void guardar(Pedido pedido) {
        pedidos.put(pedido.getId(), pedido);
    }
}
