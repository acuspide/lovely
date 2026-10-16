package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.DatosNegocio;
import com.example.ecommerce.domain.entity.Factura;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.UsuarioNoEncontradoException;
import com.example.ecommerce.domain.repository.FacturaRepository;
import com.example.ecommerce.domain.repository.UsuarioRepository;

import java.time.Clock;
import java.util.Optional;

/**
 * F-08: reglas de emisión y anulación de facturas que comparten varios casos
 * de uso (avanzar el estado del pedido, cancelarlo y descargar la factura).
 * Siempre se llama dentro de una transacción que ya tiene el pedido
 * bloqueado, así que dos personas no pueden emitir la misma factura a la vez
 * (RN15); la restricción UNIQUE de la base de datos es la última barrera.
 */
public class EmisorFacturas {

    private final FacturaRepository facturaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DatosNegocio negocio;
    private final Clock reloj;

    public EmisorFacturas(FacturaRepository facturaRepository, UsuarioRepository usuarioRepository,
                          DatosNegocio negocio, Clock reloj) {
        this.facturaRepository = facturaRepository;
        this.usuarioRepository = usuarioRepository;
        this.negocio = negocio;
        this.reloj = reloj;
    }

    /**
     * RF-08.1 / RF-08.7: devuelve la factura del pedido y, si el pedido ya se
     * confirmó y todavía no la tiene, la emite. Un pedido Pendiente o
     * cancelado antes de confirmarse no recibe factura.
     */
    public Optional<Factura> emitirSiCorresponde(Pedido pedido) {
        Optional<Factura> existente = facturaRepository.buscarPorPedido(pedido.getId());
        if (existente.isPresent() || !pedido.getEstado().yaFueConfirmado()) {
            return existente;
        }
        Usuario clienta = usuarioRepository.obtenerPorId(pedido.getClienteId())
                .orElseThrow(UsuarioNoEncontradoException::new);
        // RF-08.3: el teléfono es el de contacto del pedido (obligatorio), que es
        // al que la tienda llama para la entrega.
        Factura nueva = Factura.emitir(pedido.getId(), clienta.getNombre(), clienta.getEmail().getValor(),
                pedido.getEntrega().getTelefono().getValor(), negocio.tasaIva(), reloj.instant());
        return Optional.of(facturaRepository.guardar(nueva));
    }

    /** RF-08.6: si el pedido cancelado tenía factura, queda anulada. */
    public void anularSiExiste(Pedido pedido) {
        facturaRepository.buscarPorPedido(pedido.getId()).ifPresent(factura -> {
            factura.anular(reloj.instant());
            facturaRepository.guardar(factura);
        });
    }
}
