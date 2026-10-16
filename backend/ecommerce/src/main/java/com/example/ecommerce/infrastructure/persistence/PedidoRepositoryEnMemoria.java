package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.exception.PedidoDuplicadoException;
import com.example.ecommerce.domain.repository.PedidoRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Doble de pruebas del puerto PedidoRepository (tests unitarios de los casos
 * de uso). El adaptador de producción es PedidoRepositoryJpaAdapter (Oracle).
 */
public class PedidoRepositoryEnMemoria implements PedidoRepository {

    private final Map<Long, Pedido> pedidos = new LinkedHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    @Override
    public Optional<Pedido> obtenerPorId(long id) {
        return Optional.ofNullable(pedidos.get(id));
    }

    @Override
    public Optional<Pedido> buscarPorClaveIdempotencia(long clienteId, String claveIdempotencia) {
        return pedidos.values().stream()
                .filter(p -> p.getClienteId() == clienteId && p.getClaveIdempotencia().equalsIgnoreCase(claveIdempotencia))
                .findFirst();
    }

    @Override
    public Pedido guardar(Pedido pedido) {
        if (pedido.getId() == 0) {
            if (buscarPorClaveIdempotencia(pedido.getClienteId(), pedido.getClaveIdempotencia()).isPresent()) {
                throw new PedidoDuplicadoException(null); // igual que la restricción UNIQUE de Oracle
            }
            pedido = pedido.conId(secuencia.incrementAndGet());
        }
        pedidos.put(pedido.getId(), pedido);
        return pedido;
    }

    @Override
    public boolean existePedidoActivoConArticulo(long articuloId) {
        return pedidos.values().stream().anyMatch(p -> p.estaActivo() && p.incluyeArticulo(articuloId));
    }
}
