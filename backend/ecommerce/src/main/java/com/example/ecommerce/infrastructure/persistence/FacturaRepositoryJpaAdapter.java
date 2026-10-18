package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Factura;
import com.example.ecommerce.domain.exception.FacturaDuplicadaException;
import com.example.ecommerce.domain.repository.FacturaRepository;
import com.example.ecommerce.infrastructure.persistence.jpa.FacturaJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.Optional;

/** Patrón: Adapter — implementa el puerto FacturaRepository con Spring Data y Oracle (F-08). */
@Repository
public class FacturaRepositoryJpaAdapter implements FacturaRepository {

    private static final String RESTRICCION_PEDIDO = "UQ_FACTURAS_PEDIDO";

    private final FacturaJpaRepository jpaRepository;

    public FacturaRepositoryJpaAdapter(FacturaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Factura> buscarPorPedido(long pedidoId) {
        return jpaRepository.findByPedidoId(pedidoId).map(FacturaMapper::aDominio);
    }

    /** saveAndFlush: para traducir aquí mismo la violación de "una factura por pedido" (RN15). */
    @Override
    public Factura guardar(Factura factura) {
        try {
            return FacturaMapper.aDominio(jpaRepository.saveAndFlush(FacturaMapper.aEntidad(factura)));
        } catch (DataIntegrityViolationException ex) {
            if (String.valueOf(ex.getMostSpecificCause().getMessage()).toUpperCase(Locale.ROOT)
                    .contains(RESTRICCION_PEDIDO)) {
                throw new FacturaDuplicadaException(ex);
            }
            throw ex;
        }
    }
}
