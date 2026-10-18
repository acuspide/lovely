package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.exception.PedidoDuplicadoException;
import com.example.ecommerce.domain.exception.PedidoNoEncontradoException;
import com.example.ecommerce.domain.repository.PedidoRepository;
import com.example.ecommerce.domain.valueobject.EstadoPedido;
import com.example.ecommerce.infrastructure.persistence.jpa.PedidoJpaEntity;
import com.example.ecommerce.infrastructure.persistence.jpa.PedidoJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Patrón: Adapter — implementa el puerto PedidoRepository con Spring Data y
 * Oracle (F-05 y F-07).
 */
@Repository
public class PedidoRepositoryJpaAdapter implements PedidoRepository {

    private static final String RESTRICCION_IDEMPOTENCIA = "UQ_PEDIDOS_IDEMPOTENCIA";

    private final PedidoJpaRepository jpaRepository;

    public PedidoRepositoryJpaAdapter(PedidoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Pedido> obtenerPorId(long id) {
        return jpaRepository.findById(id).map(PedidoMapper::aDominio);
    }

    @Override
    public Optional<Pedido> obtenerParaActualizar(long id) {
        return jpaRepository.findByIdParaActualizar(id).map(PedidoMapper::aDominio);
    }

    @Override
    public List<Pedido> listar(Optional<EstadoPedido> estado) {
        List<PedidoJpaEntity> filas = estado
                .map(e -> jpaRepository.findByEstadoOrderByCreadoEnDescIdDesc(e.name()))
                .orElseGet(jpaRepository::findAllByOrderByCreadoEnDescIdDesc);
        return filas.stream().map(PedidoMapper::aDominio).toList();
    }

    @Override
    public List<Pedido> listarPorCliente(long clienteId) {
        return jpaRepository.findByClienteIdOrderByCreadoEnDescIdDesc(clienteId).stream()
                .map(PedidoMapper::aDominio).toList();
    }

    @Override
    public Optional<Pedido> buscarPorClaveIdempotencia(long clienteId, String claveIdempotencia) {
        return jpaRepository.findByClienteIdAndClaveIdempotencia(clienteId, claveIdempotencia.toLowerCase(Locale.ROOT))
                .map(PedidoMapper::aDominio);
    }

    /**
     * saveAndFlush: la escritura se hace aquí mismo y no al final de la
     * transacción, para poder traducir la violación de la restricción UNIQUE
     * del intento de compra (RF-05.7) a una excepción del dominio.
     */
    @Override
    public Pedido guardar(Pedido pedido) {
        if (pedido.getId() != 0) {
            return actualizarEstado(pedido);
        }
        try {
            return PedidoMapper.aDominio(jpaRepository.saveAndFlush(PedidoMapper.aEntidad(pedido)));
        } catch (DataIntegrityViolationException ex) {
            if (String.valueOf(ex.getMostSpecificCause().getMessage()).toUpperCase(Locale.ROOT)
                    .contains(RESTRICCION_IDEMPOTENCIA)) {
                throw new PedidoDuplicadoException(ex);
            }
            throw ex;
        }
    }

    /**
     * F-07: en un pedido existente solo cambian el estado y el historial (que
     * solo crece). Se actualiza la fila ya cargada en la transacción en lugar
     * de reemplazar el pedido completo, para no borrar y reinsertar sus detalles.
     */
    private Pedido actualizarEstado(Pedido pedido) {
        PedidoJpaEntity entidad = jpaRepository.findById(pedido.getId())
                .orElseThrow(PedidoNoEncontradoException::new);
        var historial = pedido.getHistorial();
        int yaGuardadas = Math.min(entidad.getHistorial().size(), historial.size());
        entidad.actualizarEstado(pedido.getEstado().name(),
                PedidoMapper.aFilasHistorial(historial.subList(yaGuardadas, historial.size())));
        return PedidoMapper.aDominio(jpaRepository.saveAndFlush(entidad));
    }

    @Override
    public boolean existePedidoActivoConArticulo(long articuloId) {
        return jpaRepository.existePedidoActivoConArticulo(articuloId);
    }
}
