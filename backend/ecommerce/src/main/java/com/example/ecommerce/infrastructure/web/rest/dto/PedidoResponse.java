package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.valueobject.DatosEntrega;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Patrón: DTO — pedido para la confirmación (RF-05.5) y su consulta. */
public record PedidoResponse(
        long id,
        String numero,
        String estado,
        Instant creadoEn,
        BigDecimal total,
        int unidades,
        List<LineaResponse> lineas,
        EntregaResponse entrega) {

    public static PedidoResponse desde(Pedido pedido) {
        DatosEntrega entrega = pedido.getEntrega();
        return new PedidoResponse(
                pedido.getId(),
                pedido.getNumero().valor(),
                pedido.getEstado().name(),
                pedido.getCreadoEn(),
                pedido.calcularTotal(),
                pedido.cantidadDeUnidades(),
                pedido.getDetalles().stream()
                        .map(d -> new LineaResponse(d.getArticuloId(), d.getNombreArticulo(), d.getCantidad(),
                                d.getPrecioUnitario().valor(), d.subtotal()))
                        .toList(),
                new EntregaResponse(entrega.getTipo().name(), entrega.getNombreContacto(),
                        entrega.getTelefono().getValor(), entrega.getDireccion(), entrega.getCiudad(),
                        entrega.getIndicaciones()));
    }

    public record LineaResponse(long articuloId, String nombre, int cantidad, BigDecimal precioUnitario,
                                BigDecimal subtotal) {
    }

    public record EntregaResponse(String tipo, String nombreContacto, String telefono, String direccion,
                                  String ciudad, String indicaciones) {
    }
}
