package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Factura;
import com.example.ecommerce.domain.valueobject.EstadoFactura;
import com.example.ecommerce.infrastructure.persistence.jpa.FacturaJpaEntity;

/** Patrón: Mapper — traduce entre Factura (dominio) y FacturaJpaEntity (persistencia). */
public final class FacturaMapper {

    private FacturaMapper() {
    }

    public static Factura aDominio(FacturaJpaEntity entidad) {
        return new Factura(entidad.getId(), entidad.getPedidoId(), FechasUtc.aInstante(entidad.getEmitidaEn()),
                entidad.getNombreClienta(), entidad.getEmailClienta(), entidad.getTelefonoClienta(),
                entidad.getTasaIva(), EstadoFactura.valueOf(entidad.getEstado()),
                FechasUtc.aInstante(entidad.getAnuladaEn()));
    }

    public static FacturaJpaEntity aEntidad(Factura factura) {
        // id = null cuando la factura aún no existe: Oracle lo asigna con FACTURAS_SEQ.
        Long id = factura.getId() == 0 ? null : factura.getId();
        return new FacturaJpaEntity(id, factura.getPedidoId(), FechasUtc.aColumna(factura.getEmitidaEn()),
                factura.getEstado().name(), FechasUtc.aColumna(factura.getAnuladaEn().orElse(null)),
                factura.getNombreClienta(), factura.getEmailClienta(), factura.getTelefonoClienta().orElse(null),
                factura.getTasaIva());
    }
}
