package com.example.ecommerce.domain.repository;

import com.example.ecommerce.domain.entity.Pedido;

import java.util.Optional;

/**
 * Puerto de salida (arquitectura hexagonal) para los pedidos de las clientas
 * (F-05). En producción lo implementa el adaptador de Oracle/JPA; en las
 * pruebas, uno en memoria.
 */
public interface PedidoRepository {

    Optional<Pedido> obtenerPorId(long id);

    /**
     * RF-05.7: busca un pedido ya registrado con la misma clave de intento de
     * compra de esa clienta (reintento tras un error de conexión).
     */
    Optional<Pedido> buscarPorClaveIdempotencia(long clienteId, String claveIdempotencia);

    /**
     * Guarda el pedido y devuelve la versión guardada (con id y, por lo tanto,
     * con número de pedido si era nuevo).
     *
     * @throws com.example.ecommerce.domain.exception.PedidoDuplicadoException si
     *         ya existe un pedido con la misma clave de intento (dos envíos simultáneos)
     */
    Pedido guardar(Pedido pedido);

    /** RN17 / RF-02.9: true si algún pedido no cancelado incluye ese producto. */
    boolean existePedidoActivoConArticulo(long articuloId);
}
