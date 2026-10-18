package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.usecase.DescargarFacturaUseCase.ArchivoFactura;
import com.example.ecommerce.application.usecase.DescargarFacturaUseCase.Solicitante;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.FacturaNoDisponibleException;
import com.example.ecommerce.domain.exception.PedidoNoEncontradoException;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.EstadoPedido;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.domain.valueobject.QuienCancela;
import com.example.ecommerce.infrastructure.persistence.FacturaRepositoryEnMemoria;
import com.example.ecommerce.infrastructure.persistence.PedidoRepositoryEnMemoria;
import com.example.ecommerce.support.EntornoAutenticacion;
import com.example.ecommerce.support.Facturas;
import com.example.ecommerce.support.Pedidos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** RF-08.4, RF-08.5 y RF-08.7: descarga de la factura. */
public class DescargarFacturaUseCaseTest {

    private static final long ADMIN = 1L;

    private EntornoAutenticacion entorno;
    private PedidoRepositoryEnMemoria pedidos;
    private FacturaRepositoryEnMemoria facturas;
    private Facturas.GeneradorPdfFalso generador;
    private DescargarFacturaUseCase useCase;
    private Usuario clienta;

    @BeforeEach
    void preparar() {
        entorno = new EntornoAutenticacion();
        pedidos = new PedidoRepositoryEnMemoria();
        facturas = new FacturaRepositoryEnMemoria();
        generador = new Facturas.GeneradorPdfFalso();
        var emisor = new EmisorFacturas(facturas, entorno.usuarios, Facturas.NEGOCIO, entorno.reloj);
        useCase = new DescargarFacturaUseCase(pedidos, facturas, emisor, generador, Facturas.NEGOCIO,
                entorno.transacciones);
        clienta = entorno.registrarCliente("camila@correo.com");
    }

    @Test
    void laClientaDescargaLaFacturaDeSuPedidoConfirmado() {
        // RF-08.4 / RF-08.7: el pedido se confirmó sin factura y se emite al pedirla
        Pedido pedido = confirmado();

        ArchivoFactura archivo = useCase.ejecutar(pedido.getId(), new Solicitante(clienta.getId(), true));

        assertEquals("Factura-FV-000001.pdf", archivo.nombreArchivo());
        assertEquals(1, facturas.cantidad());
        assertEquals(pedido.getId(), generador.generados.get(0).pedido().getId());
    }

    @Test
    void descargarVariasVecesNoCreaOtraFactura() {
        // RN15
        Pedido pedido = confirmado();

        useCase.ejecutar(pedido.getId(), new Solicitante(clienta.getId(), true));
        useCase.ejecutar(pedido.getId(), new Solicitante(ADMIN, false));

        assertEquals(1, facturas.cantidad());
        assertEquals(2, generador.generados.size());
    }

    @Test
    void otraClientaNoPuedeDescargarla() {
        // RF-17
        Pedido pedido = confirmado();
        Usuario otra = entorno.registrarCliente("otra@correo.com");

        assertThrows(PedidoNoEncontradoException.class,
                () -> useCase.ejecutar(pedido.getId(), new Solicitante(otra.getId(), true)));
        assertEquals(0, facturas.cantidad());
    }

    @Test
    void unPedidoPendienteTodaviaNoTieneFactura() {
        // RF-08.1
        Pedido pedido = pedidos.guardar(nuevo());

        assertThrows(FacturaNoDisponibleException.class,
                () -> useCase.ejecutar(pedido.getId(), new Solicitante(ADMIN, false)));
    }

    @Test
    void unPedidoCanceladoSinConfirmarNuncaTieneFactura() {
        Pedido pedido = nuevo();
        pedido.cancelar(QuienCancela.CLIENTA, clienta.getId(), "Me equivoqué", Pedidos.AHORA);
        Pedido cancelado = pedidos.guardar(pedido);

        assertThrows(FacturaNoDisponibleException.class,
                () -> useCase.ejecutar(cancelado.getId(), new Solicitante(ADMIN, false)));
    }

    private Pedido nuevo() {
        return Pedidos.pendiente(clienta.getId(),
                new DetallePedido(1L, "Labial", 2, new Precio(new BigDecimal("15000"))));
    }

    private Pedido confirmado() {
        Pedido pedido = nuevo();
        pedido.avanzarA(EstadoPedido.CONFIRMADO, ADMIN, Pedidos.AHORA);
        return pedidos.guardar(pedido);
    }
}
