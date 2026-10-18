package com.example.ecommerce.domain.repository;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.valueobject.EstadoPedido;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (arquitectura hexagonal) para los pedidos de las clientas
 * (F-05 y F-07). En producción lo implementa el adaptador de Oracle/JPA; en las
 * pruebas, uno en memoria.
 */
public interface PedidoRepository {

    Optional<Pedido> obtenerPorId(long id);

    /**
     * F-07: como {@link #obtenerPorId(long)}, pero bloquea el pedido hasta que
     * termine la transacción, para que dos personas no cambien su estado a la
     * vez (por ejemplo, la clienta cancela mientras la asesora lo confirma).
     */
    Optional<Pedido> obtenerParaActualizar(long id);

    /** RF-07.1: todos los pedidos (o solo los de un estado), del más reciente al más antiguo. */
    List<Pedido> listar(Optional<EstadoPedido> estado);

    /** RF-07.8: pedidos de una clienta, del más reciente al más antiguo. */
    List<Pedido> listarPorCliente(long clienteId);

    /**
     * RF-05.7: busca un pedido ya registrado con la misma clave de intento de
     * compra de esa clienta (reintento tras un error de conexión).
     */
    Optional<Pedido> buscarPorClaveIdempotencia(long clienteId, String claveIdempotencia);

    /**
     * Guarda el pedido y devuelve la versión guardada (con id y, por lo tanto,
     * con número de pedido si era nuevo). Si ya existía, guarda su nuevo estado
     * y las entradas nuevas del historial (F-07).
     *
     * @throws com.example.ecommerce.domain.exception.PedidoDuplicadoException si
     *         ya existe un pedido con la misma clave de intento (dos envíos simultáneos)
     */
    Pedido guardar(Pedido pedido);

    /** RN17 / RF-02.9: true si algún pedido no cancelado incluye ese producto. */
    boolean existePedidoActivoConArticulo(long articuloId);
}
