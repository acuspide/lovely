package com.example.ecommerce.domain.repository;

import com.example.ecommerce.domain.entity.Pedido;

import java.util.Optional;

public interface PedidoRepository {
    Optional<Pedido> obtenerPorId(long id);

    void guardar(Pedido pedido);

    /**
     * RN17: true si existe algún pedido no cancelado que incluya este
     * artículo en sus detalles. Lo usa EliminarArticuloUseCase (F-02) aunque
     * el feature de "Realizar Pedido" todavía no tenga REST/Angular propios.
     */
    boolean existePedidoActivoConArticulo(long articuloId);
}
