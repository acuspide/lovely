package com.example.ecommerce.domain.repository;

import com.example.ecommerce.domain.entity.Pedido;

import java.util.Optional;

public interface PedidoRepository {
    Optional<Pedido> obtenerPorId(long id);

    void guardar(Pedido pedido);
}
