package com.example.ecommerce.integracion;

import com.example.ecommerce.application.usecase.ConfirmarPagoUseCase;
import com.example.ecommerce.application.usecase.CrearArticuloUseCase;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase.Linea;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase.Solicitud;
import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.repository.PedidoRepository;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.EstadoPedido;
import com.example.ecommerce.domain.valueobject.MedioPago;
import com.example.ecommerce.domain.valueobject.PagoConfirmado;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.infrastructure.persistence.jpa.ArticuloJpaRepository;
import com.example.ecommerce.infrastructure.persistence.jpa.FacturaJpaRepository;
import com.example.ecommerce.infrastructure.persistence.jpa.PedidoJpaRepository;
import com.example.ecommerce.support.Pedidos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F-09 contra Oracle real: la migración V10 y el mapeo JPA guardan la
 * confirmación del pago, y la factura se emite en la misma transacción.
 *
 * <p>Necesita Oracle levantado y las cuentas de demostración. Crea un
 * producto con nombre único y lo borra al terminar, con su pedido y factura.</p>
 */
@SpringBootTest
class ConfirmacionPagoOracleTest {

    @Autowired private RegistrarPedidoUseCase registrarPedidoUseCase;
    @Autowired private ConfirmarPagoUseCase confirmarPagoUseCase;
    @Autowired private CrearArticuloUseCase crearArticuloUseCase;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ArticuloJpaRepository articuloJpaRepository;
    @Autowired private PedidoJpaRepository pedidoJpaRepository;
    @Autowired private FacturaJpaRepository facturaJpaRepository;

    private Articulo producto;
    private Long pedidoId;

    @Test
    void laConfirmacionDelPagoQuedaGuardadaYGeneraLaFactura() {
        producto = crearArticuloUseCase.ejecutar("Prueba pago " + UUID.randomUUID(), "Producto de prueba",
                new Precio(new BigDecimal("12500")), CategoriaArticulo.FACIAL, null, null, 5);
        Usuario clienta = usuarioRepository.buscarPorEmail(new Email("cliente@lovelygirl.com")).orElseThrow();
        Usuario asesora = usuarioRepository.buscarPorEmail(new Email("asesora@lovelygirl.com")).orElseThrow();
        Pedido pedido = registrarPedidoUseCase.ejecutar(new Solicitud(clienta.getId(),
                List.of(new Linea(producto.getId(), 2)), Pedidos.retiroEnTienda(), Pedidos.claveNueva())).pedido();
        pedidoId = pedido.getId();
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Bogota"));

        confirmarPagoUseCase.ejecutar(new ConfirmarPagoUseCase.Solicitud(pedidoId, MedioPago.TRANSFERENCIA,
                "TRX-ORACLE-01", new BigDecimal("25000"), hoy, asesora.getId()));

        Pedido leido = pedidoRepository.obtenerPorId(pedidoId).orElseThrow();
        assertEquals(EstadoPedido.CONFIRMADO, leido.getEstado());
        PagoConfirmado pago = leido.getPago().orElseThrow();
        assertEquals(MedioPago.TRANSFERENCIA, pago.medio());
        assertEquals("TRX-ORACLE-01", pago.referencia());
        assertEquals(0, new BigDecimal("25000").compareTo(pago.monto()));
        assertEquals(hoy, pago.fechaRecepcion());
        assertEquals(asesora.getId(), pago.confirmadoPor());
        assertTrue(facturaJpaRepository.findByPedidoId(pedidoId).isPresent(), "se emitió la factura");
    }

    @AfterEach
    void limpiar() {
        if (pedidoId != null) {
            facturaJpaRepository.findByPedidoId(pedidoId).ifPresent(facturaJpaRepository::delete);
            pedidoJpaRepository.deleteById(pedidoId);
        }
        if (producto != null) {
            articuloJpaRepository.deleteById(producto.getId());
        }
    }
}
