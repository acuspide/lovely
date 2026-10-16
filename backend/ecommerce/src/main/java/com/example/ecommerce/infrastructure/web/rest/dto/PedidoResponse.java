package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.valueobject.CambioEstadoPedido;
import com.example.ecommerce.domain.valueobject.DatosEntrega;
import com.example.ecommerce.domain.valueobject.QuienCancela;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Patrón: DTO — pedido de la clienta: confirmación (RF-05.5), consulta y
 * seguimiento de su estado (RF-07.8). No incluye quién hizo cada cambio: eso
 * solo lo ve el personal ({@link PedidoGestionResponse}).
 *
 * @param puedeCancelar     RF-07.5: la clienta puede cancelarlo (solo si está Pendiente)
 * @param motivoCancelacion null si no está cancelado
 */
public record PedidoResponse(
        long id,
        String numero,
        String estado,
        Instant creadoEn,
        BigDecimal total,
        int unidades,
        List<LineaResponse> lineas,
        EntregaResponse entrega,
        boolean puedeCancelar,
        String motivoCancelacion,
        List<CambioEstadoResponse> historial) {

    public static PedidoResponse desde(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getNumero().valor(),
                pedido.getEstado().name(),
                pedido.getCreadoEn(),
                pedido.calcularTotal(),
                pedido.cantidadDeUnidades(),
                lineasDe(pedido),
                EntregaResponse.desde(pedido.getEntrega()),
                QuienCancela.CLIENTA.puedeCancelarDesde(pedido.getEstado()),
                pedido.getMotivoCancelacion().orElse(null),
                pedido.getHistorial().stream().map(CambioEstadoResponse::desde).toList());
    }

    static List<LineaResponse> lineasDe(Pedido pedido) {
        return pedido.getDetalles().stream()
                .map(d -> new LineaResponse(d.getArticuloId(), d.getNombreArticulo(), d.getCantidad(),
                        d.getPrecioUnitario().valor(), d.subtotal()))
                .toList();
    }

    public record LineaResponse(long articuloId, String nombre, int cantidad, BigDecimal precioUnitario,
                                BigDecimal subtotal) {
    }

    public record EntregaResponse(String tipo, String nombreContacto, String telefono, String direccion,
                                  String ciudad, String indicaciones) {

        static EntregaResponse desde(DatosEntrega entrega) {
            return new EntregaResponse(entrega.getTipo().name(), entrega.getNombreContacto(),
                    entrega.getTelefono().getValor(), entrega.getDireccion(), entrega.getCiudad(),
                    entrega.getIndicaciones());
        }
    }

    /** RF-07.8: seguimiento para la clienta (sin el nombre de quien hizo el cambio). */
    public record CambioEstadoResponse(String estadoAnterior, String estadoNuevo, Instant fecha, String motivo) {

        static CambioEstadoResponse desde(CambioEstadoPedido cambio) {
            return new CambioEstadoResponse(
                    cambio.estadoAnterior() == null ? null : cambio.estadoAnterior().name(),
                    cambio.estadoNuevo().name(), cambio.fecha(), cambio.motivo());
        }
    }
}
