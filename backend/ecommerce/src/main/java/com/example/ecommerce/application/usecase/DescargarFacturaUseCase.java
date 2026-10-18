package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.DatosNegocio;
import com.example.ecommerce.application.DocumentoFactura;
import com.example.ecommerce.application.GeneradorPdfFactura;
import com.example.ecommerce.application.GestorTransacciones;
import com.example.ecommerce.domain.entity.Factura;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.exception.FacturaNoDisponibleException;
import com.example.ecommerce.domain.exception.PedidoNoEncontradoException;
import com.example.ecommerce.domain.repository.FacturaRepository;
import com.example.ecommerce.domain.repository.PedidoRepository;
import com.example.ecommerce.domain.valueobject.EstadoPedido;

import java.util.Objects;
import java.util.Optional;

/**
 * RF-08.4 / RF-08.5 / RF-08.7: descarga de la factura en PDF.
 *
 * <ul>
 *   <li>La clienta solo descarga las facturas de sus propios pedidos; si pide
 *       la de otro pedido se responde como si no existiera (RF-17).</li>
 *   <li>La Administradora descarga la de cualquier pedido (el rol se controla
 *       en el endpoint).</li>
 *   <li>Si el pedido ya estaba confirmado antes de existir la facturación y
 *       no tiene factura, se emite en ese momento (RF-08.7).</li>
 * </ul>
 *
 * <p>El PDF se arma cada vez a partir de los datos guardados de la factura y
 * del pedido, que no cambian, así que siempre sale igual (decisión 3 de F-08).
 * Se genera fuera de la transacción para no mantener bloqueos mientras se dibuja.</p>
 */
public class DescargarFacturaUseCase {

    /** Quién pide la factura. */
    public record Solicitante(long usuarioId, boolean esClienta) {
    }

    /** El PDF listo para enviar al navegador. */
    public record ArchivoFactura(String nombreArchivo, byte[] contenido) {
        public ArchivoFactura {
            Objects.requireNonNull(nombreArchivo, "nombreArchivo");
            Objects.requireNonNull(contenido, "contenido");
        }
    }

    private final PedidoRepository pedidoRepository;
    private final FacturaRepository facturaRepository;
    private final EmisorFacturas emisorFacturas;
    private final GeneradorPdfFactura generadorPdf;
    private final DatosNegocio negocio;
    private final GestorTransacciones transacciones;

    public DescargarFacturaUseCase(PedidoRepository pedidoRepository, FacturaRepository facturaRepository,
                                   EmisorFacturas emisorFacturas, GeneradorPdfFactura generadorPdf,
                                   DatosNegocio negocio, GestorTransacciones transacciones) {
        this.pedidoRepository = pedidoRepository;
        this.facturaRepository = facturaRepository;
        this.emisorFacturas = emisorFacturas;
        this.generadorPdf = generadorPdf;
        this.negocio = negocio;
        this.transacciones = transacciones;
    }

    /**
     * @throws PedidoNoEncontradoException  si no existe o, para la clienta, si no es suyo
     * @throws FacturaNoDisponibleException si el pedido sigue Pendiente o se canceló sin confirmarse
     */
    public ArchivoFactura ejecutar(long pedidoId, Solicitante solicitante) {
        Pedido pedido = visible(pedidoRepository.obtenerPorId(pedidoId), solicitante);
        Factura factura = facturaRepository.buscarPorPedido(pedidoId)
                .orElseGet(() -> emitirAhora(pedidoId, solicitante));

        byte[] pdf = generadorPdf.generar(new DocumentoFactura(negocio, factura, pedido));
        return new ArchivoFactura("Factura-" + factura.getNumero().valor() + ".pdf", pdf);
    }

    /** RF-08.7: emite la factura que falta, con el pedido bloqueado para no duplicarla. */
    private Factura emitirAhora(long pedidoId, Solicitante solicitante) {
        return transacciones.enTransaccion(() -> {
            Pedido pedido = visible(pedidoRepository.obtenerParaActualizar(pedidoId), solicitante);
            return emisorFacturas.emitirSiCorresponde(pedido)
                    .orElseThrow(() -> new FacturaNoDisponibleException(pedido.getEstado() == EstadoPedido.CANCELADO
                            ? "Este pedido se canceló antes de confirmarse, así que no tiene factura."
                            : "La factura estará disponible cuando la tienda confirme el pedido."));
        });
    }

    private static Pedido visible(Optional<Pedido> pedido, Solicitante solicitante) {
        return pedido
                .filter(p -> !solicitante.esClienta() || p.getClienteId() == solicitante.usuarioId())
                .orElseThrow(PedidoNoEncontradoException::new);
    }
}
