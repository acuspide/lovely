package com.example.ecommerce.infrastructure.config;

import com.example.ecommerce.application.usecase.ActualizarArticuloUseCase;
import com.example.ecommerce.application.usecase.CambiarEstadoArticuloUseCase;
import com.example.ecommerce.application.usecase.CrearArticuloUseCase;
import com.example.ecommerce.application.usecase.EliminarArticuloUseCase;
import com.example.ecommerce.application.usecase.ListarArticulosUseCase;
import com.example.ecommerce.application.usecase.ObtenerArticuloUseCase;
import com.example.ecommerce.application.usecase.RevisarCarritoUseCase;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.repository.PedidoRepository;
import com.example.ecommerce.infrastructure.persistence.PedidoRepositoryEnMemoria;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Cablea como beans de Spring las piezas de dominio/aplicación (casos de uso
 * y puertos), que a propósito son clases planas de Java sin anotaciones de
 * framework. Las de F-01 están en AutenticacionConfig.
 *
 * Los repositorios con adaptador JPA no se declaran aquí: ya son
 * {@code @Repository} de Spring y se inyectan directamente.
 */
@Configuration
public class AppConfig {

    /**
     * Reloj único de la aplicación, en UTC. Los casos de uso lo reciben en vez
     * de llamar a {@code Instant.now()}, lo que permite probar reglas que
     * dependen del tiempo (bloqueo de 15 min, sesión de 30 min, enlaces de
     * recuperación) sin esperar en los tests.
     */
    @Bean
    public Clock reloj() {
        return Clock.systemUTC();
    }

    /**
     * F-02 usa PedidoRepository solo para la verificación de solo lectura de
     * RN17 (¿tiene pedidos activos?). El feature "Realizar Pedido" (con su
     * propio adaptador Oracle) todavía no existe, así que por ahora el único
     * adaptador es este de memoria — intencional, no un descuido.
     */
    @Bean
    public PedidoRepository pedidoRepository() {
        return new PedidoRepositoryEnMemoria();
    }

    @Bean
    public CrearArticuloUseCase crearArticuloUseCase(ArticuloRepository articuloRepository) {
        return new CrearArticuloUseCase(articuloRepository);
    }

    @Bean
    public ActualizarArticuloUseCase actualizarArticuloUseCase(ArticuloRepository articuloRepository) {
        return new ActualizarArticuloUseCase(articuloRepository);
    }

    @Bean
    public EliminarArticuloUseCase eliminarArticuloUseCase(ArticuloRepository articuloRepository,
                                                           PedidoRepository pedidoRepository) {
        return new EliminarArticuloUseCase(articuloRepository, pedidoRepository);
    }

    @Bean
    public CambiarEstadoArticuloUseCase cambiarEstadoArticuloUseCase(ArticuloRepository articuloRepository) {
        return new CambiarEstadoArticuloUseCase(articuloRepository);
    }

    @Bean
    public RevisarCarritoUseCase revisarCarritoUseCase(ArticuloRepository articuloRepository) {
        return new RevisarCarritoUseCase(articuloRepository);
    }

    @Bean
    public ListarArticulosUseCase listarArticulosUseCase(ArticuloRepository articuloRepository) {
        return new ListarArticulosUseCase(articuloRepository);
    }

    @Bean
    public ObtenerArticuloUseCase obtenerArticuloUseCase(ArticuloRepository articuloRepository) {
        return new ObtenerArticuloUseCase(articuloRepository);
    }
}
