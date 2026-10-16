package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase.Linea;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase.Resultado;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase.Solicitud;
import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.ArticuloAgotadoException;
import com.example.ecommerce.domain.exception.ArticuloInactivoException;
import com.example.ecommerce.domain.exception.StockInsuficienteException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.EstadoPedido;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.infrastructure.persistence.ArticuloRepositoryEnMemoria;
import com.example.ecommerce.infrastructure.persistence.PedidoRepositoryEnMemoria;
import com.example.ecommerce.support.EntornoAutenticacion;
import com.example.ecommerce.support.NotificadorPedidosFalso;
import com.example.ecommerce.support.Pedidos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** F-05: registro de pedidos (RF-05.1 a RF-05.7). */
class RegistrarPedidoUseCaseTest {

    private final EntornoAutenticacion entorno = new EntornoAutenticacion();
    private final ArticuloRepository articulos = new ArticuloRepositoryEnMemoria();
    private final PedidoRepositoryEnMemoria pedidos = new PedidoRepositoryEnMemoria();
    private final NotificadorPedidosFalso correo = new NotificadorPedidosFalso();
    private RegistrarPedidoUseCase useCase;
    private Usuario clienta;
    private Articulo labial;   // 20.000, stock 10
    private Articulo base;     // 30.000, stock 2

    @BeforeEach
    void preparar() {
        clienta = entorno.registrarCliente("ana@correo.com");
        CrearArticuloUseCase crear = new CrearArticuloUseCase(articulos);
        labial = crear.ejecutar("Labial Mate", "Descripción", new Precio(new BigDecimal("20000")),
                CategoriaArticulo.MAQUILLAJE, null, null, 10);
        base = crear.ejecutar("Base Líquida", "Descripción", new Precio(new BigDecimal("30000")),
                CategoriaArticulo.FACIAL, null, null, 2);
        useCase = new RegistrarPedidoUseCase(articulos, pedidos, entorno.usuarios, correo,
                entorno.transacciones, entorno.reloj);
    }

    private Solicitud solicitud(String clave, Linea... lineas) {
        return new Solicitud(clienta.getId(), List.of(lineas), Pedidos.retiroEnTienda(), clave);
    }

    @Test
    void registraElPedidoPendienteConNumeroYPreciosDelMomento() {
        // Act
        Resultado resultado = useCase.ejecutar(solicitud(Pedidos.claveNueva(),
                new Linea(labial.getId(), 2), new Linea(base.getId(), 1)));

        // Assert (RF-05.1 a RF-05.3)
        Pedido pedido = resultado.pedido();
        assertTrue(resultado.nuevo());
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertEquals("LG-000001", pedido.getNumero().valor());
        assertEquals(clienta.getId(), pedido.getClienteId());
        assertEquals(0, new BigDecimal("70000").compareTo(pedido.calcularTotal()));
        assertEquals("Labial Mate", pedido.getDetalles().get(0).getNombreArticulo());
    }

    @Test
    void losNumerosDePedidoSonConsecutivos() {
        // RF-05.2
        Pedido primero = useCase.ejecutar(solicitud(Pedidos.claveNueva(), new Linea(labial.getId(), 1))).pedido();
        Pedido segundo = useCase.ejecutar(solicitud(Pedidos.claveNueva(), new Linea(labial.getId(), 1))).pedido();

        assertEquals("LG-000001", primero.getNumero().valor());
        assertEquals("LG-000002", segundo.getNumero().valor());
    }

    @Test
    void noRegistraNadaSiUnProductoNoAlcanza() {
        // RF-05.4 / RN09: la base solo tiene 2 unidades
        assertThrows(StockInsuficienteException.class, () -> useCase.ejecutar(solicitud(Pedidos.claveNueva(),
                new Linea(labial.getId(), 1), new Linea(base.getId(), 3))));

        assertTrue(pedidos.buscarPorClaveIdempotencia(clienta.getId(), "x").isEmpty());
        assertTrue(correo.enviados().isEmpty());
    }

    @Test
    void noRegistraProductosAgotadosNiDesactivados() {
        // RF-05.4 / RN01
        Articulo agotado = new CrearArticuloUseCase(articulos).ejecutar("Aceite", "Descripción",
                new Precio(new BigDecimal("15000")), CategoriaArticulo.CAPILAR, null, null, 0);
        new CambiarEstadoArticuloUseCase(articulos).ejecutar(labial.getId(), false);

        assertThrows(ArticuloAgotadoException.class,
                () -> useCase.ejecutar(solicitud(Pedidos.claveNueva(), new Linea(agotado.getId(), 1))));
        assertThrows(ArticuloInactivoException.class,
                () -> useCase.ejecutar(solicitud(Pedidos.claveNueva(), new Linea(labial.getId(), 1))));
    }

    @Test
    void unReintentoConLaMismaClaveDevuelveElMismoPedidoSinDuplicar() {
        // RF-05.7 / CU-02 A5
        String clave = Pedidos.claveNueva();
        Resultado primero = useCase.ejecutar(solicitud(clave, new Linea(labial.getId(), 1)));

        Resultado reintento = useCase.ejecutar(solicitud(clave, new Linea(labial.getId(), 1)));

        assertFalse(reintento.nuevo());
        assertEquals(primero.pedido().getId(), reintento.pedido().getId());
        assertEquals(1, correo.enviados().size(), "el correo no se envía dos veces");
    }

    @Test
    void enviaElCorreoDeConfirmacion() {
        // RF-05.6
        Pedido pedido = useCase.ejecutar(solicitud(Pedidos.claveNueva(), new Linea(labial.getId(), 1))).pedido();

        assertEquals(List.of("ana@correo.com " + pedido.getNumero().valor()), correo.enviados());
    }

    @Test
    void unProductoRepetidoQuedaEnUnSoloDetalle() {
        Pedido pedido = useCase.ejecutar(solicitud(Pedidos.claveNueva(),
                new Linea(labial.getId(), 1), new Linea(labial.getId(), 2))).pedido();

        assertEquals(1, pedido.getDetalles().size());
        assertEquals(3, pedido.getDetalles().get(0).getCantidad());
    }

    @Test
    void conPedidosRegistradosElProductoYaNoSePuedeEliminar() {
        // RF-02.9 ahora con pedidos reales
        useCase.ejecutar(solicitud(Pedidos.claveNueva(), new Linea(base.getId(), 1)));

        assertTrue(pedidos.existePedidoActivoConArticulo(base.getId()));
        assertFalse(pedidos.existePedidoActivoConArticulo(labial.getId()));
    }

    // ---------------- F-06: descuento automático de inventario ----------------

    @Test
    void alRegistrarElPedidoSeDescuentaElStock() {
        // RF-06.1
        useCase.ejecutar(solicitud(Pedidos.claveNueva(), new Linea(labial.getId(), 3), new Linea(base.getId(), 1)));

        assertEquals(7, articulos.obtenerPorId(labial.getId()).orElseThrow().getStock());
        assertEquals(1, articulos.obtenerPorId(base.getId()).orElseThrow().getStock());
    }

    @Test
    void siUnProductoNoAlcanzaNoSeDescuentaNingunoNiSeRegistraElPedido() {
        // RF-06.2 (CO-02: todo o nada) y RF-06.4
        StockInsuficienteException error = assertThrows(StockInsuficienteException.class,
                () -> useCase.ejecutar(solicitud(Pedidos.claveNueva(),
                        new Linea(labial.getId(), 3), new Linea(base.getId(), 5))));

        assertEquals(10, articulos.obtenerPorId(labial.getId()).orElseThrow().getStock(), "el labial no se tocó");
        assertEquals(2, articulos.obtenerPorId(base.getId()).orElseThrow().getStock());
        assertTrue(error.getMessage().contains("Solo quedan 2 unidades de «Base Líquida»"));
        assertFalse(pedidos.existePedidoActivoConArticulo(labial.getId()));
    }

    @Test
    void alVenderLasUltimasUnidadesElProductoQuedaAgotadoEnElCatalogo() {
        // RF-06.6 / RN12
        useCase.ejecutar(solicitud(Pedidos.claveNueva(), new Linea(base.getId(), 2)));

        Articulo enCatalogo = new ListarArticulosUseCase(articulos)
                .ejecutar(java.util.Optional.empty(), java.util.Optional.of("Base")).get(0);
        assertTrue(enCatalogo.estaAgotado());
    }

    @Test
    void despuesDeAgotarseUnaSegundaCompraRecibeElAviso() {
        // RF-06.5 (versión secuencial; la concurrente está en PedidoConcurrenteIT con Oracle)
        useCase.ejecutar(solicitud(Pedidos.claveNueva(), new Linea(base.getId(), 2)));

        assertThrows(ArticuloAgotadoException.class,
                () -> useCase.ejecutar(solicitud(Pedidos.claveNueva(), new Linea(base.getId(), 1))));
        assertEquals(0, articulos.obtenerPorId(base.getId()).orElseThrow().getStock(), "nunca negativo (RN20)");
    }

    @Test
    void unReintentoNoDescuentaElStockDosVeces() {
        // RF-05.7 + RF-06.1
        String clave = Pedidos.claveNueva();
        useCase.ejecutar(solicitud(clave, new Linea(labial.getId(), 2)));
        useCase.ejecutar(solicitud(clave, new Linea(labial.getId(), 2)));

        assertEquals(8, articulos.obtenerPorId(labial.getId()).orElseThrow().getStock());
    }
}
