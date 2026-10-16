package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.exception.PedidoDuplicadoException;
import com.example.ecommerce.domain.repository.PedidoRepository;
import com.example.ecommerce.domain.valueobject.EstadoPedido;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
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

    private static final Comparator<Pedido> MAS_RECIENTE_PRIMERO =
            Comparator.comparing(Pedido::getCreadoEn).thenComparingLong(Pedido::getId).reversed();

    @Override
    public Optional<Pedido> obtenerPorId(long id) {
        return Optional.ofNullable(pedidos.get(id));
    }

    @Override
    public Optional<Pedido> obtenerParaActualizar(long id) {
        return obtenerPorId(id);
    }

    @Override
    public List<Pedido> listar(Optional<EstadoPedido> estado) {
        return pedidos.values().stream()
                .filter(p -> estado.map(e -> p.getEstado() == e).orElse(true))
                .sorted(MAS_RECIENTE_PRIMERO)
                .toList();
    }

    @Override
    public List<Pedido> listarPorCliente(long clienteId) {
        return pedidos.values().stream()
                .filter(p -> p.getClienteId() == clienteId)
                .sorted(MAS_RECIENTE_PRIMERO)
                .toList();
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
