package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.valueobject.CambioEstadoPedido;
import com.example.ecommerce.domain.valueobject.DatosEntrega;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.EstadoPedido;
import com.example.ecommerce.domain.valueobject.MedioPago;
import com.example.ecommerce.domain.valueobject.PagoConfirmado;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.domain.valueobject.TipoEntrega;
import com.example.ecommerce.infrastructure.persistence.jpa.DetallePedidoJpaEntity;
import com.example.ecommerce.infrastructure.persistence.jpa.HistorialEstadoPedidoJpaEntity;
import com.example.ecommerce.infrastructure.persistence.jpa.PedidoJpaEntity;

import java.util.List;

/** Patrón: Mapper — traduce entre Pedido (dominio) y PedidoJpaEntity (persistencia). */
public final class PedidoMapper {

    private PedidoMapper() {
    }

    public static Pedido aDominio(PedidoJpaEntity entidad) {
        var detalles = entidad.getDetalles().stream()
                .map(d -> new DetallePedido(d.getArticuloId(), d.getNombreArticulo(), d.getCantidad(),
                        new Precio(d.getPrecioUnitario())))
                .toList();
        var entrega = new DatosEntrega(TipoEntrega.valueOf(entidad.getTipoEntrega()), entidad.getNombreContacto(),
                entidad.getTelefonoContacto(), entidad.getDireccion(), entidad.getCiudad(), entidad.getIndicaciones());
        return new Pedido(entidad.getId(), entidad.getClienteId(), detalles, entrega,
                FechasUtc.aInstante(entidad.getCreadoEn()), entidad.getClaveIdempotencia(),
                EstadoPedido.valueOf(entidad.getEstado()),
                entidad.getHistorial().stream().map(PedidoMapper::aCambio).toList(), aPago(entidad));
    }

    /** F-09: null si el pedido todavía no tiene pago confirmado. */
    private static PagoConfirmado aPago(PedidoJpaEntity entidad) {
        if (entidad.getPagoMedio() == null) {
            return null;
        }
        return new PagoConfirmado(MedioPago.valueOf(entidad.getPagoMedio()), entidad.getPagoReferencia(),
                entidad.getPagoMonto(), entidad.getPagoFechaRecepcion(), entidad.getPagoConfirmadoPor(),
                FechasUtc.aInstante(entidad.getPagoConfirmadoEn()));
    }

    /** F-09: copia el pago del dominio a la fila, si el pedido lo tiene. */
    public static void copiarPago(Pedido pedido, PedidoJpaEntity entidad) {
        pedido.getPago().ifPresent(pago -> entidad.registrarPago(pago.medio().name(), pago.referencia(),
                pago.monto(), pago.fechaRecepcion(), pago.confirmadoPor(), FechasUtc.aColumna(pago.confirmadoEn())));
    }

    private static CambioEstadoPedido aCambio(HistorialEstadoPedidoJpaEntity fila) {
        return new CambioEstadoPedido(
                fila.getEstadoAnterior() == null ? null : EstadoPedido.valueOf(fila.getEstadoAnterior()),
                EstadoPedido.valueOf(fila.getEstadoNuevo()), FechasUtc.aInstante(fila.getFecha()),
                fila.getUsuarioId(), fila.getMotivo());
    }

    /** RF-07.7: convierte entradas del historial del dominio a filas de la tabla. */
    public static List<HistorialEstadoPedidoJpaEntity> aFilasHistorial(List<CambioEstadoPedido> cambios) {
        return cambios.stream()
                .map(c -> new HistorialEstadoPedidoJpaEntity(
                        c.estadoAnterior() == null ? null : c.estadoAnterior().name(), c.estadoNuevo().name(),
                        FechasUtc.aColumna(c.fecha()), c.usuarioId(), c.motivo()))
                .toList();
    }

    public static PedidoJpaEntity aEntidad(Pedido pedido) {
        // id = null cuando el pedido aún no existe: Oracle lo asigna con PEDIDOS_SEQ.
        Long id = pedido.getId() == 0 ? null : pedido.getId();
        var detalles = pedido.getDetalles().stream()
                .map(d -> new DetallePedidoJpaEntity(d.getArticuloId(), d.getNombreArticulo(), d.getCantidad(),
                        d.getPrecioUnitario().valor()))
                .toList();
        DatosEntrega entrega = pedido.getEntrega();
        PedidoJpaEntity entidad = new PedidoJpaEntity(id, pedido.getClienteId(), pedido.getEstado().name(),
                FechasUtc.aColumna(pedido.getCreadoEn()), pedido.getClaveIdempotencia(), entrega.getTipo().name(),
                entrega.getNombreContacto(), entrega.getTelefono().getValor(), entrega.getDireccion(),
                entrega.getCiudad(), entrega.getIndicaciones(), detalles, aFilasHistorial(pedido.getHistorial()));
        copiarPago(pedido, entidad);
        return entidad;
    }
}
