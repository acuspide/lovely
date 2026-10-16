package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.exception.CancelacionNoPermitidaException;
import com.example.ecommerce.domain.exception.MotivoCancelacionInvalidoException;
import com.example.ecommerce.domain.exception.PedidoNoEncontradoException;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.EstadoPedido;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.domain.valueobject.QuienCancela;
import com.example.ecommerce.infrastructure.persistence.ArticuloRepositoryEnMemoria;
import com.example.ecommerce.infrastructure.persistence.PedidoRepositoryEnMemoria;
import com.example.ecommerce.support.GestorTransaccionesDirecto;
import com.example.ecommerce.support.Pedidos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** RF-07.5 / RF-07.6: cancelar con motivo y devolver el stock. */
public class CancelarPedidoUseCaseTest {

    private static final long CLIENTA = 100L;
    private static final long ADMIN = 1L;

    private ArticuloRepositoryEnMemoria articulos;
    private PedidoRepositoryEnMemoria pedidos;
    private CancelarPedidoUseCase useCase;
    private Articulo labial;
    private Articulo base;

    @BeforeEach
    void preparar() {
        articulos = new ArticuloRepositoryEnMemoria();
        pedidos = new PedidoRepositoryEnMemoria();
        useCase = new CancelarPedidoUseCase(pedidos, articulos, new GestorTransaccionesDirecto(),
                Clock.fixed(Pedidos.AHORA, ZoneOffset.UTC));
        labial = crear("Labial Mate", 10);
        base = crear("Base Líquida", 5);
    }

    @Test
    void laClientaCancelaSuPedidoPendienteYElStockVuelve() {
        // Arrange: el pedido descontó 3 labiales y 2 bases (F-06)
        Pedido pedido = registrarVendiendo(3, 2);

        // Act
        Pedido cancelado = useCase.ejecutar(
                new CancelarPedidoUseCase.Solicitud(pedido.getId(), QuienCancela.CLIENTA, CLIENTA, "Me equivoqué"));

        // Assert
        assertEquals(EstadoPedido.CANCELADO, cancelado.getEstado());
        assertEquals("Me equivoqué", cancelado.getMotivoCancelacion().orElseThrow());
        assertEquals(10, stockDe(labial), "RF-07.6: vuelven los 3 labiales");
        assertEquals(5, stockDe(base), "RF-07.6: vuelven las 2 bases");
    }

    @Test
    void laAdministradoraCancelaUnPedidoConfirmado() {
        Pedido pedido = registrarVendiendo(1, 0);
        pedido.avanzarA(EstadoPedido.CONFIRMADO, ADMIN, Pedidos.AHORA);
        pedidos.guardar(pedido);

        useCase.ejecutar(new CancelarPedidoUseCase.Solicitud(
                pedido.getId(), QuienCancela.ADMINISTRADORA, ADMIN, "Pago rechazado"));

        assertEquals(EstadoPedido.CANCELADO, pedidos.obtenerPorId(pedido.getId()).orElseThrow().getEstado());
        assertEquals(10, stockDe(labial));
    }

    @Test
    void laClientaNoPuedeCancelarUnPedidoConfirmadoNiSeTocaElStock() {
        Pedido pedido = registrarVendiendo(2, 0);
        pedido.avanzarA(EstadoPedido.CONFIRMADO, ADMIN, Pedidos.AHORA);
        pedidos.guardar(pedido);

        assertThrows(CancelacionNoPermitidaException.class, () -> useCase.ejecutar(
                new CancelarPedidoUseCase.Solicitud(pedido.getId(), QuienCancela.CLIENTA, CLIENTA, "Ya no")));
        assertEquals(8, stockDe(labial));
    }

    @Test
    void unaClientaNoPuedeCancelarElPedidoDeOtra() {
        Pedido pedido = registrarVendiendo(1, 0);

        assertThrows(PedidoNoEncontradoException.class, () -> useCase.ejecutar(
                new CancelarPedidoUseCase.Solicitud(pedido.getId(), QuienCancela.CLIENTA, 999L, "No es mío")));
        assertEquals(EstadoPedido.PENDIENTE, pedidos.obtenerPorId(pedido.getId()).orElseThrow().getEstado());
    }

    @Test
    void sinMotivoNoSeCancela() {
        Pedido pedido = registrarVendiendo(1, 0);

        assertThrows(MotivoCancelacionInvalidoException.class, () -> useCase.ejecutar(
                new CancelarPedidoUseCase.Solicitud(pedido.getId(), QuienCancela.CLIENTA, CLIENTA, "")));
        assertEquals(9, stockDe(labial));
    }

    // ---- apoyo ----

    private Articulo crear(String nombre, int stock) {
        return new CrearArticuloUseCase(articulos).ejecutar(nombre, "Descripción",
                new Precio(new BigDecimal("20000")), CategoriaArticulo.MAQUILLAJE, null, null, stock);
    }

    /** Simula F-06: descuenta el stock y guarda un pedido Pendiente con esas unidades. */
    private Pedido registrarVendiendo(int labiales, int bases) {
        var detalles = new java.util.ArrayList<DetallePedido>();
        if (labiales > 0) {
            vender(labial, labiales);
            detalles.add(new DetallePedido(labial.getId(), "Labial Mate", labiales, labial.getPrecio()));
        }
        if (bases > 0) {
            vender(base, bases);
            detalles.add(new DetallePedido(base.getId(), "Base Líquida", bases, base.getPrecio()));
        }
        return pedidos.guardar(Pedidos.pendiente(CLIENTA, detalles.toArray(DetallePedido[]::new)));
    }

    private void vender(Articulo articulo, int unidades) {
        Articulo actual = articulos.obtenerPorId(articulo.getId()).orElseThrow();
        actual.descontarStock(unidades);
        articulos.guardar(actual);
    }

    private int stockDe(Articulo articulo) {
        return articulos.obtenerPorId(articulo.getId()).orElseThrow().getStock();
    }
}
