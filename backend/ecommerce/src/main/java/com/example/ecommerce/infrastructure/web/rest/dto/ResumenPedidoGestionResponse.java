package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.application.usecase.VistaPedido;
import com.example.ecommerce.domain.entity.Pedido;

import java.math.BigDecimal;
import java.time.Instant;

/** Patrón: DTO — una fila de la lista de pedidos del personal (RF-07.1 / IU-04). */
public record ResumenPedidoGestionResponse(
        long id,
        String numero,
        String clienta,
        Instant creadoEn,
        BigDecimal total,
        String estado,
        String tipoEntrega) {

    public static ResumenPedidoGestionResponse desde(VistaPedido vista) {
        Pedido pedido = vista.pedido();
        return new ResumenPedidoGestionResponse(pedido.getId(), pedido.getNumero().valor(), vista.nombreClienta(),
                pedido.getCreadoEn(), pedido.calcularTotal(), pedido.getEstado().name(),
                pedido.getEntrega().getTipo().name());
    }
}
