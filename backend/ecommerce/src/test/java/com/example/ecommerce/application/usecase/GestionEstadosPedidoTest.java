package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.PedidoNoEncontradoException;
import com.example.ecommerce.domain.exception.TransicionEstadoInvalidaException;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.EstadoPedido;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.infrastructure.persistence.PedidoRepositoryEnMemoria;
import com.example.ecommerce.support.EntornoAutenticacion;
import com.example.ecommerce.support.Pedidos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF-07.1, RF-07.2, RF-07.3, RF-07.7 y RF-07.8: lista, detalle y avance de estados. */
public class GestionEstadosPedidoTest {

    private static final Precio PRECIO = new Precio(new BigDecimal("15000"));

    private EntornoAutenticacion entorno;
    private PedidoRepositoryEnMemoria pedidos;
    private Usuario clienta;
    private Usuario asesora;

    @BeforeEach
    void preparar() {
        entorno = new EntornoAutenticacion();
        pedidos = new PedidoRepositoryEnMemoria();
        clienta = entorno.registrarCliente("camila@correo.com");
        asesora = entorno.crearPersonal("asesora@lovelygirl.com", RolUsuario.ASESORA_VENTAS);
    }

    @Test
    void laAsesoraAvanzaElPedidoYQuedaEnElHistorialConSuNombre() {
        // RF-07.3 / RF-07.7
        Pedido pedido = registrar();
        var avanzar = new AvanzarEstadoPedidoUseCase(pedidos, entorno.transacciones, entorno.reloj);

        avanzar.ejecutar(pedido.getId(), EstadoPedido.CONFIRMADO, asesora.getId());
        VistaPedido vista = new ObtenerPedidoParaGestionUseCase(pedidos, entorno.usuarios).ejecutar(pedido.getId());

        assertEquals(EstadoPedido.CONFIRMADO, vista.pedido().getEstado());
        var ultimo = vista.pedido().getHistorial().get(1);
        assertEquals(EstadoPedido.PENDIENTE, ultimo.estadoAnterior());
        assertEquals(asesora.getNombre(), vista.nombreDe(ultimo.usuarioId()));
        assertEquals(clienta.getNombre(), vista.nombreClienta());
    }

    @Test
    void siOtraPersonaYaCambioElEstadoElPasoSeRechaza() {
        // RF-07.3: dos personas con la misma pantalla abierta
        Pedido pedido = registrar();
        var avanzar = new AvanzarEstadoPedidoUseCase(pedidos, entorno.transacciones, entorno.reloj);
        avanzar.ejecutar(pedido.getId(), EstadoPedido.CONFIRMADO, asesora.getId());

        assertThrows(TransicionEstadoInvalidaException.class,
                () -> avanzar.ejecutar(pedido.getId(), EstadoPedido.CONFIRMADO, asesora.getId()));
    }

    @Test
    void avanzarUnPedidoInexistenteResponde404() {
        var avanzar = new AvanzarEstadoPedidoUseCase(pedidos, entorno.transacciones, entorno.reloj);

        assertThrows(PedidoNoEncontradoException.class,
                () -> avanzar.ejecutar(999L, EstadoPedido.CONFIRMADO, asesora.getId()));
    }

    @Test
    void laListaFiltraPorEstadoYBuscaPorNumeroONombre() {
        // RF-07.1
        Pedido primero = registrar();
        Pedido segundo = registrar();
        new AvanzarEstadoPedidoUseCase(pedidos, entorno.transacciones, entorno.reloj)
                .ejecutar(segundo.getId(), EstadoPedido.CONFIRMADO, asesora.getId());
        var listar = new ListarPedidosUseCase(pedidos, entorno.usuarios);

        assertEquals(2, listar.ejecutar(Optional.empty(), null).size());
        assertEquals(List.of(segundo.getId()), ids(listar.ejecutar(Optional.of(EstadoPedido.CONFIRMADO), "")));
        assertEquals(List.of(primero.getId()), ids(listar.ejecutar(Optional.empty(), primero.getNumero().valor())));
        assertEquals(List.of(primero.getId()), ids(listar.ejecutar(Optional.empty(), String.valueOf(primero.getId()))));
        assertEquals(2, listar.ejecutar(Optional.empty(), "camila rios").size(), "sin tildes ni mayúsculas");
        assertTrue(listar.ejecutar(Optional.empty(), "nadie").isEmpty());
    }

    @Test
    void laClientaSoloVeSusPedidos() {
        // RF-07.8
        registrar();
        Usuario otra = entorno.registrarCliente("otra@correo.com");
        pedidos.guardar(Pedidos.pendiente(otra.getId(), new DetallePedido(1L, "Labial", 1, PRECIO)));

        var mios = new ListarPedidosDeClienteUseCase(pedidos).ejecutar(clienta.getId());

        assertEquals(1, mios.size());
        assertEquals(clienta.getId(), mios.get(0).getClienteId());
    }

    private Pedido registrar() {
        return pedidos.guardar(Pedidos.pendiente(clienta.getId(), new DetallePedido(1L, "Labial", 1, PRECIO)));
    }

    private static List<Long> ids(List<VistaPedido> vistas) {
        return vistas.stream().map(v -> v.pedido().getId()).toList();
    }
}
