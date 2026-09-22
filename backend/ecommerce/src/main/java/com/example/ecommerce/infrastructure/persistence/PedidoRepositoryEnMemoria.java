package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.repository.PedidoRepository;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.EstadoPedido;

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

    @Override
    public boolean existePedidoActivoConArticulo(long articuloId) {
        return pedidos.values().stream()
                .filter(pedido -> pedido.getEstado() != EstadoPedido.CANCELADO)
                .flatMap(pedido -> pedido.getDetalles().stream())
                .map(DetallePedido::getArticuloId)
                .anyMatch(id -> id == articuloId);
    }
}
