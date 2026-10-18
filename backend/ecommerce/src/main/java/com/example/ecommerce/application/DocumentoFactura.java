package com.example.ecommerce.application;

import com.example.ecommerce.domain.entity.Factura;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.valueobject.DesgloseIva;

import java.util.Objects;

/**
 * RF-08.3: todo lo que lleva el PDF de la factura: datos del negocio, la
 * factura (número, fecha, clienta, estado), el pedido (productos, cantidades,
 * precios, entrega) y el desglose de IVA.
 */
public record DocumentoFactura(DatosNegocio negocio, Factura factura, Pedido pedido) {

    public DocumentoFactura {
        Objects.requireNonNull(negocio, "negocio");
        Objects.requireNonNull(factura, "factura");
        Objects.requireNonNull(pedido, "pedido");
    }

    public DesgloseIva desglose() {
        return factura.desglosar(pedido);
    }
}
