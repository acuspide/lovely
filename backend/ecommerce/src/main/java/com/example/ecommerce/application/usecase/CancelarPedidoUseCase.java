package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.GestorTransacciones;
import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.exception.PedidoNoEncontradoException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.repository.PedidoRepository;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.QuienCancela;

import java.time.Clock;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * RF-07.5 / RF-07.6: cancela un pedido con su motivo y devuelve al inventario
 * las unidades que se habían descontado al registrarlo (F-06).
 *
 * <ul>
 *   <li>La clienta cancela sus propios pedidos, solo mientras están Pendientes.</li>
 *   <li>La Administradora cancela pedidos Pendientes o Confirmados.</li>
 *   <li>La Asesora de ventas no cancela (se controla en el endpoint).</li>
 * </ul>
 *
 * <p>RF-08.6: si el pedido ya tenía factura (estaba Confirmado), la factura
 * queda anulada.</p>
 *
 * <p>Todo ocurre en una sola transacción: si algo falla, ni el pedido queda
 * cancelado ni el stock cambia. El pedido se bloquea primero (para que nadie
 * lo avance mientras tanto) y luego los productos en orden de id, igual que
 * al registrar pedidos, para evitar bloqueos cruzados.</p>
 */
public class CancelarPedidoUseCase {

    /**
     * @param usuarioId cuenta que cancela; si es la clienta, debe ser la dueña del pedido
     * @param motivo    RF-07.5: obligatorio
     */
    public record Solicitud(long pedidoId, QuienCancela quien, long usuarioId, String motivo) {
        public Solicitud {
            Objects.requireNonNull(quien, "quien");
        }
    }

    private final PedidoRepository pedidoRepository;
    private final ArticuloRepository articuloRepository;
    private final EmisorFacturas emisorFacturas;
    private final GestorTransacciones transacciones;
    private final Clock reloj;

    public CancelarPedidoUseCase(PedidoRepository pedidoRepository, ArticuloRepository articuloRepository,
                                 EmisorFacturas emisorFacturas, GestorTransacciones transacciones, Clock reloj) {
        this.pedidoRepository = pedidoRepository;
        this.articuloRepository = articuloRepository;
        this.emisorFacturas = emisorFacturas;
        this.transacciones = transacciones;
        this.reloj = reloj;
    }

    /**
     * @throws PedidoNoEncontradoException si no existe o, para la clienta, si no es suyo
     *         (responde igual para no revelar pedidos ajenos, como en RF-05.5)
     */
    public Pedido ejecutar(Solicitud solicitud) {
        return transacciones.enTransaccion(() -> {
            Pedido pedido = pedidoRepository.obtenerParaActualizar(solicitud.pedidoId())
                    .filter(p -> solicitud.quien() != QuienCancela.CLIENTA
                            || p.getClienteId() == solicitud.usuarioId())
                    .orElseThrow(PedidoNoEncontradoException::new);

            pedido.cancelar(solicitud.quien(), solicitud.usuarioId(), solicitud.motivo(), reloj.instant());
            devolverStock(pedido);
            emisorFacturas.anularSiExiste(pedido);
            return pedidoRepository.guardar(pedido);
        });
    }

    /** RF-07.6: suma de nuevo las unidades de cada producto del pedido. */
    private void devolverStock(Pedido pedido) {
        Map<Long, Integer> unidadesPorArticulo = new TreeMap<>();
        for (DetallePedido detalle : pedido.getDetalles()) {
            unidadesPorArticulo.merge(detalle.getArticuloId(), detalle.getCantidad(), Integer::sum);
        }
        unidadesPorArticulo.forEach((articuloId, unidades) -> {
            // Un producto con pedidos activos no se puede eliminar (RF-02.9), así
            // que debería existir; si no, no hay inventario al cual devolver.
            articuloRepository.obtenerParaActualizar(articuloId).ifPresent(articulo -> reponer(articulo, unidades));
        });
    }

    private void reponer(Articulo articulo, int unidades) {
        articulo.reponerStock(unidades);
        articuloRepository.guardar(articulo);
    }
}
