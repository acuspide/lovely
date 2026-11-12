package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.GestorTransacciones;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.exception.PedidoNoEncontradoException;
import com.example.ecommerce.domain.repository.PedidoRepository;
import com.example.ecommerce.domain.valueobject.MedioPago;
import com.example.ecommerce.domain.valueobject.PagoConfirmado;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * F-09 / RN11: la Asesora de ventas o la Administradora confirman que
 * Lovely Girl recibió el dinero de un pedido Pendiente.
 *
 * <ul>
 *   <li>RF-09.1: lo hace el personal después de verificar la cuenta bancaria
 *       o de recibir el efectivo; el sistema no procesa pagos en línea.</li>
 *   <li>RF-09.2: se guardan medio, referencia, monto, fecha de recepción,
 *       quién confirmó y cuándo.</li>
 *   <li>RF-09.5: el pedido pasa a Confirmado y se emite su factura en la misma
 *       transacción: o quedan las dos cosas o ninguna.</li>
 * </ul>
 *
 * <p>El pedido se lee bloqueado: si la clienta lo cancela al mismo tiempo,
 * una de las dos operaciones espera y la segunda encuentra el estado real.</p>
 */
public class ConfirmarPagoUseCase {

    private final PedidoRepository pedidoRepository;
    private final EmisorFacturas emisorFacturas;
    private final GestorTransacciones transacciones;
    private final Clock reloj;

    public ConfirmarPagoUseCase(PedidoRepository pedidoRepository, EmisorFacturas emisorFacturas,
                                GestorTransacciones transacciones, Clock reloj) {
        this.pedidoRepository = pedidoRepository;
        this.emisorFacturas = emisorFacturas;
        this.transacciones = transacciones;
        this.reloj = reloj;
    }

    /**
     * @param usuarioId cuenta del personal que confirma (queda en el pago y en el historial)
     */
    public record Solicitud(long pedidoId, MedioPago medio, String referencia, BigDecimal monto,
                            LocalDate fechaRecepcion, long usuarioId) {
    }

    /**
     * @throws PedidoNoEncontradoException si el pedido no existe
     * @throws com.example.ecommerce.domain.exception.PagoInvalidoException
     *         si faltan datos, el monto no es el total o la fecha no es válida
     * @throws com.example.ecommerce.domain.exception.TransicionEstadoInvalidaException
     *         si el pedido ya no está Pendiente
     */
    public Pedido ejecutar(Solicitud solicitud) {
        return transacciones.enTransaccion(() -> {
            Pedido pedido = pedidoRepository.obtenerParaActualizar(solicitud.pedidoId())
                    .orElseThrow(PedidoNoEncontradoException::new);
            pedido.confirmarPago(new PagoConfirmado(solicitud.medio(), solicitud.referencia(), solicitud.monto(),
                    solicitud.fechaRecepcion(), solicitud.usuarioId(), reloj.instant()));
            Pedido guardado = pedidoRepository.guardar(pedido);
            emisorFacturas.emitirSiCorresponde(guardado);
            return guardado;
        });
    }
}
