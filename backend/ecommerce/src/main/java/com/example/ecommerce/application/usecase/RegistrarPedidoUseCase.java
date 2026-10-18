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
import java.util.TreeMap;

/**
 * F-05: registrar el pedido de una clienta (CU-02 pasos 11 y 12).
 *
 * <ol>
 *   <li>RF-05.7 / CU-02 A5: si ese intento de compra ya se registró (la
 *       clienta reintentó tras un error de conexión), devuelve el mismo
 *       pedido en vez de crear otro.</li>
 *   <li>RF-05.4 / RN01 / RN05 / RN09 / RF-06.4: cada producto debe estar
 *       activo, tener stock y alcanzar para la cantidad pedida; si no, no se
 *       registra nada y se informa cuántas unidades quedan.</li>
 *   <li>RF-05.1 a RF-05.3: guarda el pedido en estado Pendiente, con el precio
 *       y nombre de cada producto en ese momento y los datos de entrega.</li>
 *   <li>RF-05.6: después de guardar, envía el correo de confirmación.</li>
 * </ol>
 *
 * <p>F-06 (CO-02 descontarStock): en la misma transacción se descuenta el
 * stock de cada producto (RF-06.1). O se guardan el pedido y todos los
 * descuentos, o no se guarda nada (RF-06.2). Cada producto se bloquea mientras
 * se descuenta, para que dos compras simultáneas no vendan la misma última
 * unidad (RF-06.5).</p>
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
        Map<Long, Integer> cantidades = cantidadesPorArticulo(solicitud.lineas());

        // 1. Bloquear y verificar TODOS los productos antes de tocar ninguno.
        //    Se bloquean en orden de id: si dos pedidos comparten productos, los
        //    piden en el mismo orden y no se quedan esperándose el uno al otro.
        Map<Long, Articulo> bloqueados = new TreeMap<>();
        for (long articuloId : new TreeMap<>(cantidades).keySet()) {
            bloqueados.put(articuloId, bloquearYVerificar(articuloId, cantidades.get(articuloId)));
        }

        // 2. RF-06.1: descontar. Ya se sabe que todo alcanza, así que no puede
        //    quedar un descuento a medias (RF-06.2); la transacción es la red de seguridad.
        bloqueados.forEach((articuloId, articulo) -> {
            articulo.descontarStock(cantidades.get(articuloId));
            articuloRepository.guardar(articulo);
        });

        // 3. RF-05.3: los detalles conservan el orden del carrito y congelan nombre y precio.
        List<DetallePedido> detalles = cantidades.entrySet().stream()
                .map(entrada -> {
                    Articulo articulo = bloqueados.get(entrada.getKey());
                    return new DetallePedido(entrada.getKey(), articulo.getNombre().getValor(),
                            entrada.getValue(), articulo.getPrecio());
                })
                .toList();

        Pedido pedido = Pedido.registrar(solicitud.clienteId(), detalles, solicitud.entrega(),
                reloj.instant(), solicitud.claveIdempotencia());
        return pedidoRepository.guardar(pedido);
    }

    /**
     * RF-05.4 / RF-06.4 / RF-06.5: bloquea el producto (para que nadie más lo
     * descuente al mismo tiempo) y verifica que esté a la venta y alcance.
     */
    private Articulo bloquearYVerificar(long articuloId, int cantidad) {
        Articulo articulo = articuloRepository.obtenerParaActualizar(articuloId)
                .orElseThrow(ArticuloNoEncontradoException::new);
        articulo.verificarDisponibleParaVenta();
        if (cantidad > articulo.getStock()) {
            throw new StockInsuficienteException(articulo.getNombre().getValor(), articulo.getStock());
        }
        return articulo;
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
