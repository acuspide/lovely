package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.GestorTransacciones;
import com.example.ecommerce.application.NotificadorPedidos;
import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.ArticuloNoEncontradoException;
import com.example.ecommerce.domain.exception.PedidoDuplicadoException;
import com.example.ecommerce.domain.exception.StockInsuficienteException;
import com.example.ecommerce.domain.exception.UsuarioNoEncontradoException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.repository.PedidoRepository;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.DatosEntrega;
import com.example.ecommerce.domain.valueobject.DetallePedido;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * F-05: registrar el pedido de una clienta (CU-02 pasos 11 y 12).
 *
 * <ol>
 *   <li>RF-05.7 / CU-02 A5: si ese intento de compra ya se registró (la
 *       clienta reintentó tras un error de conexión), devuelve el mismo
 *       pedido en vez de crear otro.</li>
 *   <li>RF-05.4 / RN01 / RN05 / RN09: cada producto debe estar activo, tener
 *       stock y alcanzar para la cantidad pedida; si no, no se registra nada.</li>
 *   <li>RF-05.1 a RF-05.3: guarda el pedido en estado Pendiente, con el precio
 *       y nombre de cada producto en ese momento y los datos de entrega.</li>
 *   <li>RF-05.6: después de guardar, envía el correo de confirmación.</li>
 * </ol>
 *
 * <p>Todo ocurre dentro de una transacción. F-06 agregará aquí el descuento
 * del stock, para que pedido y stock cambien juntos o no cambie ninguno.</p>
 */
public class RegistrarPedidoUseCase {

    private final ArticuloRepository articuloRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificadorPedidos notificador;
    private final GestorTransacciones transacciones;
    private final Clock reloj;

    public RegistrarPedidoUseCase(ArticuloRepository articuloRepository, PedidoRepository pedidoRepository,
                                  UsuarioRepository usuarioRepository, NotificadorPedidos notificador,
                                  GestorTransacciones transacciones, Clock reloj) {
        this.articuloRepository = articuloRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificador = notificador;
        this.transacciones = transacciones;
        this.reloj = reloj;
    }

    public Resultado ejecutar(Solicitud solicitud) {
        Usuario clienta = usuarioRepository.obtenerPorId(solicitud.clienteId())
                .orElseThrow(UsuarioNoEncontradoException::new);

        // RF-05.7: reintento del mismo intento de compra.
        var existente = pedidoRepository.buscarPorClaveIdempotencia(solicitud.clienteId(), solicitud.claveIdempotencia());
        if (existente.isPresent()) {
            return new Resultado(existente.get(), false);
        }

        Pedido registrado;
        try {
            registrado = transacciones.enTransaccion(() -> registrarEnTransaccion(solicitud));
        } catch (PedidoDuplicadoException dosEnviosALaVez) {
            // Dos envíos simultáneos del mismo intento: el otro ganó; se responde con ese pedido.
            return new Resultado(pedidoRepository
                    .buscarPorClaveIdempotencia(solicitud.clienteId(), solicitud.claveIdempotencia())
                    .orElseThrow(() -> dosEnviosALaVez), false);
        }

        // RF-05.6: fuera de la transacción; si el correo falla, el pedido sigue registrado.
        notificador.enviarConfirmacion(clienta, registrado);
        return new Resultado(registrado, true);
    }

    private Pedido registrarEnTransaccion(Solicitud solicitud) {
        List<DetallePedido> detalles = cantidadesPorArticulo(solicitud.lineas()).entrySet().stream()
                .map(entrada -> detalleVerificado(entrada.getKey(), entrada.getValue()))
                .toList();

        Pedido pedido = Pedido.registrar(solicitud.clienteId(), detalles, solicitud.entrega(),
                reloj.instant(), solicitud.claveIdempotencia());
        return pedidoRepository.guardar(pedido);
    }

    /** RF-05.4: verifica el producto y congela su nombre y precio actuales en el detalle. */
    private DetallePedido detalleVerificado(long articuloId, int cantidad) {
        Articulo articulo = articuloRepository.obtenerPorId(articuloId)
                .orElseThrow(ArticuloNoEncontradoException::new);
        articulo.verificarDisponibleParaVenta();
        if (cantidad > articulo.getStock()) {
            throw new StockInsuficienteException(articulo.getNombre().getValor(), articulo.getStock());
        }
        return new DetallePedido(articuloId, articulo.getNombre().getValor(), cantidad, articulo.getPrecio());
    }

    /** Si un producto viene repetido, se suman sus cantidades (un detalle por producto). */
    private static Map<Long, Integer> cantidadesPorArticulo(List<Linea> lineas) {
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (Linea linea : lineas) {
            cantidades.merge(linea.articuloId(), linea.cantidad(), Integer::sum);
        }
        return cantidades;
    }

    /** Lo que envía la clienta: quién es (sale de la sesión), qué compra, cómo lo recibe y la clave del intento. */
    public record Solicitud(long clienteId, List<Linea> lineas, DatosEntrega entrega, String claveIdempotencia) {
        public Solicitud {
            lineas = List.copyOf(lineas);
        }
    }

    public record Linea(long articuloId, int cantidad) {
    }

    /**
     * @param pedido el pedido registrado
     * @param nuevo  false si era un reintento y se devolvió el pedido ya existente
     */
    public record Resultado(Pedido pedido, boolean nuevo) {
    }
}
