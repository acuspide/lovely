package com.example.ecommerce.integracion;

import com.example.ecommerce.application.usecase.CrearArticuloUseCase;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase.Linea;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase.Resultado;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase.Solicitud;
import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.infrastructure.persistence.jpa.ArticuloJpaRepository;
import com.example.ecommerce.infrastructure.persistence.jpa.PedidoJpaRepository;
import com.example.ecommerce.support.Pedidos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * F-06 / RF-06.5 contra Oracle real: dos clientas compran AL MISMO TIEMPO la
 * última unidad de un producto. Solo una debe lograrlo y el stock debe quedar
 * en 0, nunca en -1 (RN20). Esto no se puede probar con repositorios en
 * memoria, porque lo que se verifica es el bloqueo de la base de datos
 * (SELECT ... FOR UPDATE).
 *
 * <p>Necesita Oracle levantado (como EcommerceApplicationTests) y usa la
 * cuenta de demostración cliente@lovelygirl.com. Crea un producto de prueba
 * con nombre único y lo borra al terminar, junto con sus pedidos.</p>
 */
@SpringBootTest
class RegistroPedidoConcurrenteTest {

    @Autowired private RegistrarPedidoUseCase registrarPedidoUseCase;
    @Autowired private CrearArticuloUseCase crearArticuloUseCase;
    @Autowired private ArticuloRepository articuloRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ArticuloJpaRepository articuloJpaRepository;
    @Autowired private PedidoJpaRepository pedidoJpaRepository;

    private Articulo producto;
    private final List<Long> pedidosCreados = new ArrayList<>();

    @Test
    void dosComprasSimultaneasDeLaUltimaUnidadSoloVendenUna() throws Exception {
        // Arrange: un producto con UNA sola unidad
        producto = crearArticuloUseCase.ejecutar("Prueba concurrencia " + UUID.randomUUID(), "Producto de prueba",
                new Precio(new BigDecimal("10000")), CategoriaArticulo.FACIAL, null, null, 1);
        Usuario clienta = usuarioRepository.buscarPorEmail(new Email("cliente@lovelygirl.com")).orElseThrow();
        CountDownLatch largada = new CountDownLatch(1);
        Callable<Resultado> compra = () -> {
            largada.await();
            return registrarPedidoUseCase.ejecutar(new Solicitud(clienta.getId(),
                    List.of(new Linea(producto.getId(), 1)), Pedidos.retiroEnTienda(), Pedidos.claveNueva()));
        };

        // Act: las dos compras arrancan a la vez
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        Future<Resultado> primera = hilos.submit(compra);
        Future<Resultado> segunda = hilos.submit(compra);
        largada.countDown();

        int exitosas = 0;
        int rechazadas = 0;
        for (Future<Resultado> resultado : List.of(primera, segunda)) {
            try {
                pedidosCreados.add(resultado.get(30, TimeUnit.SECONDS).pedido().getId());
                exitosas++;
            } catch (java.util.concurrent.ExecutionException ex) {
                if (!(ex.getCause() instanceof ReglaDominioException)) {
                    throw ex;
                }
                rechazadas++; // agotado o stock insuficiente (RF-06.4)
            }
        }
        hilos.shutdown();

        // Assert
        assertEquals(1, exitosas, "solo una clienta se lleva la última unidad");
        assertEquals(1, rechazadas, "la otra recibe el aviso de que no hay stock");
        assertEquals(0, articuloRepository.obtenerPorId(producto.getId()).orElseThrow().getStock(),
                "el stock queda en 0, nunca en negativo");
    }

    @AfterEach
    void limpiar() {
        pedidosCreados.forEach(pedidoJpaRepository::deleteById);
        if (producto != null) {
            articuloJpaRepository.deleteById(producto.getId());
        }
    }
}
