package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.application.usecase.VistaPedido;
import com.example.ecommerce.domain.entity.Factura;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.valueobject.CambioEstadoPedido;
import com.example.ecommerce.domain.valueobject.EstadoPedido;
import com.example.ecommerce.infrastructure.web.rest.dto.PedidoResponse.EntregaResponse;
import com.example.ecommerce.infrastructure.web.rest.dto.PedidoResponse.LineaResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Patrón: DTO — detalle del pedido para el personal (RF-07.2), con el
 * historial completo (RF-07.7).
 *
 * @param siguienteEstado RF-07.3: único paso que se ofrece; null si el pedido está en un estado final
 * @param admiteCancelacion RF-07.5: el estado permite cancelar (la web solo lo ofrece a la Administradora)
 * @param factura           F-08: datos de la factura, o null si aún no tiene
 * @param facturaDisponible F-08: se puede descargar (la web solo lo ofrece a la Administradora)
 */
public record PedidoGestionResponse(
        long id,
        String numero,
        String estado,
        String siguienteEstado,
        boolean admiteCancelacion,
        Instant creadoEn,
        BigDecimal total,
        int unidades,
        ClientaResponse clienta,
        List<LineaResponse> lineas,
        EntregaResponse entrega,
        String motivoCancelacion,
        List<CambioResponse> historial,
        FacturaResponse factura,
        boolean facturaDisponible) {

    public static PedidoGestionResponse desde(VistaPedido vista, Optional<Factura> factura) {
        Pedido pedido = vista.pedido();
        EstadoPedido estado = pedido.getEstado();
        return new PedidoGestionResponse(
                pedido.getId(),
                pedido.getNumero().valor(),
                estado.name(),
                estado.siguiente().map(Enum::name).orElse(null),
                estado.admiteCancelacion(),
                pedido.getCreadoEn(),
                pedido.calcularTotal(),
                pedido.cantidadDeUnidades(),
                new ClientaResponse(pedido.getClienteId(), vista.nombreClienta()),
                PedidoResponse.lineasDe(pedido),
                EntregaResponse.desde(pedido.getEntrega()),
                pedido.getMotivoCancelacion().orElse(null),
                pedido.getHistorial().stream().map(cambio -> CambioResponse.desde(cambio, vista)).toList(),
                factura.map(FacturaResponse::desde).orElse(null),
                PedidoResponse.facturaDisponible(pedido, factura));
    }

    /** F-08: número, fecha de emisión y si está anulada (RF-08.6). */
    public record FacturaResponse(String numero, Instant emitidaEn, boolean anulada) {

        static FacturaResponse desde(Factura factura) {
            return new FacturaResponse(factura.getNumero().valor(), factura.getEmitidaEn(), factura.estaAnulada());
        }
    }

    public record ClientaResponse(long id, String nombre) {
    }

    /** RF-07.7: fecha, estado anterior, nuevo, quién lo hizo y motivo (si lo hay). */
    public record CambioResponse(Instant fecha, String estadoAnterior, String estadoNuevo, String usuario,
                                 String motivo) {

        static CambioResponse desde(CambioEstadoPedido cambio, VistaPedido vista) {
            return new CambioResponse(cambio.fecha(),
                    cambio.estadoAnterior() == null ? null : cambio.estadoAnterior().name(),
                    cambio.estadoNuevo().name(), vista.nombreDe(cambio.usuarioId()), cambio.motivo());
        }
    }
}
