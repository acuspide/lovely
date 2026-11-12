package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Factura;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.PagoInvalidoException;
import com.example.ecommerce.domain.exception.PagoNoConfirmadoException;
import com.example.ecommerce.domain.exception.PedidoNoEncontradoException;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.EstadoPedido;
import com.example.ecommerce.domain.valueobject.MedioPago;
import com.example.ecommerce.domain.valueobject.PagoConfirmado;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.infrastructure.persistence.FacturaRepositoryEnMemoria;
import com.example.ecommerce.infrastructure.persistence.PedidoRepositoryEnMemoria;
import com.example.ecommerce.support.EntornoAutenticacion;
import com.example.ecommerce.support.Facturas;
import com.example.ecommerce.support.Pedidos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** F-09 / RN11: la confirmación del pago lleva el pedido a Confirmado y emite su factura. */
public class ConfirmarPagoUseCaseTest {

    private static final Precio PRECIO = new Precio(new BigDecimal("15000"));
    private static final LocalDate DIA_PEDIDO = LocalDate.of(2026, 10, 16);

    private EntornoAutenticacion entorno;
    private PedidoRepositoryEnMemoria pedidos;
    private final FacturaRepositoryEnMemoria facturas = new FacturaRepositoryEnMemoria();
    private Usuario clienta;
    private Usuario asesora;
    private ConfirmarPagoUseCase confirmar;
    private AvanzarEstadoPedidoUseCase avanzar;

    @BeforeEach
    void preparar() {
        entorno = new EntornoAutenticacion();
        pedidos = new PedidoRepositoryEnMemoria();
        clienta = entorno.registrarCliente("camila@correo.com");
        asesora = entorno.crearPersonal("asesora@lovelygirl.com", RolUsuario.ASESORA_VENTAS);
        // Los pedidos se registran en Pedidos.AHORA; el pago se confirma una hora después.
        entorno.reloj.avanzar(Duration.between(entorno.reloj.instant(), Pedidos.AHORA.plusSeconds(3600)));
        var emisor = new EmisorFacturas(facturas, entorno.usuarios, Facturas.NEGOCIO, entorno.reloj);
        confirmar = new ConfirmarPagoUseCase(pedidos, emisor, entorno.transacciones, entorno.reloj);
        avanzar = new AvanzarEstadoPedidoUseCase(pedidos, emisor, entorno.transacciones, entorno.reloj);
    }

    @Test
    void laAsesoraConfirmaLaTransferenciaYSeEmiteLaFactura() {
        // RF-09.1 / RF-09.2 / RF-09.5 / RF-08.1
        Pedido pedido = registrar(2);

        Pedido confirmado = confirmar.ejecutar(new ConfirmarPagoUseCase.Solicitud(pedido.getId(),
                MedioPago.TRANSFERENCIA, "TRX-445566", new BigDecimal("30000"), DIA_PEDIDO, asesora.getId()));

        assertEquals(EstadoPedido.CONFIRMADO, confirmado.getEstado());
        PagoConfirmado pago = pedidos.obtenerPorId(pedido.getId()).orElseThrow().getPago().orElseThrow();
        assertEquals(MedioPago.TRANSFERENCIA, pago.medio());
        assertEquals("TRX-445566", pago.referencia());
        assertEquals(0, new BigDecimal("30000").compareTo(pago.monto()));
        assertEquals(DIA_PEDIDO, pago.fechaRecepcion());
        assertEquals(asesora.getId(), pago.confirmadoPor());
        assertEquals(entorno.reloj.instant(), pago.confirmadoEn());
        Factura factura = facturas.buscarPorPedido(pedido.getId()).orElseThrow();
        assertEquals("FV-000001", factura.getNumero().valor());
    }

    @Test
    void sinPagoConfirmadoNoHayFacturaNiSePuedeConfirmarConUnCambioDeEstado() {
        // RN11 / RF-09.6
        Pedido pedido = registrar(1);

        assertThrows(PagoNoConfirmadoException.class,
                () -> avanzar.ejecutar(pedido.getId(), EstadoPedido.CONFIRMADO, asesora.getId()));

        assertEquals(EstadoPedido.PENDIENTE, pedidos.obtenerPorId(pedido.getId()).orElseThrow().getEstado());
        assertEquals(0, facturas.cantidad());
    }

    @Test
    void unMontoDistintoAlTotalSeRechazaYElPedidoSigueSinFactura() {
        // RF-09.3: el total es $15.000
        Pedido pedido = registrar(1);

        assertThrows(PagoInvalidoException.class, () -> confirmar.ejecutar(new ConfirmarPagoUseCase.Solicitud(
                pedido.getId(), MedioPago.EFECTIVO, null, new BigDecimal("10000"), DIA_PEDIDO, asesora.getId())));

        Pedido actual = pedidos.obtenerPorId(pedido.getId()).orElseThrow();
        assertEquals(EstadoPedido.PENDIENTE, actual.getEstado());
        assertTrue(actual.getPago().isEmpty());
        assertEquals(0, facturas.cantidad());
    }

    @Test
    void confirmarElPagoDeUnPedidoInexistenteResponde404() {
        assertThrows(PedidoNoEncontradoException.class, () -> confirmar.ejecutar(new ConfirmarPagoUseCase.Solicitud(
                999L, MedioPago.EFECTIVO, null, new BigDecimal("15000"), DIA_PEDIDO, asesora.getId())));
    }

    private Pedido registrar(int cantidad) {
        return pedidos.guardar(Pedidos.pendiente(clienta.getId(), new DetallePedido(1L, "Labial", cantidad, PRECIO)));
    }
}
