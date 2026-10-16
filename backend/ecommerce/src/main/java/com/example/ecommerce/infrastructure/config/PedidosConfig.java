package com.example.ecommerce.infrastructure.config;

import com.example.ecommerce.application.GestorTransacciones;
import com.example.ecommerce.application.NotificadorPedidos;
import com.example.ecommerce.application.usecase.AvanzarEstadoPedidoUseCase;
import com.example.ecommerce.application.usecase.CancelarPedidoUseCase;
import com.example.ecommerce.application.usecase.ListarPedidosDeClienteUseCase;
import com.example.ecommerce.application.usecase.ListarPedidosUseCase;
import com.example.ecommerce.application.usecase.ObtenerPedidoDeClienteUseCase;
import com.example.ecommerce.application.usecase.ObtenerPedidoParaGestionUseCase;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.repository.PedidoRepository;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.infrastructure.correo.NotificadorPedidosSmtp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Clock;

/**
 * F-05 y F-07: arma los casos de uso de pedidos con sus adaptadores. Los casos de
 * uso son Java plano; aquí (y solo aquí) se decide qué implementación
 * recibe cada puerto.
 */
@Configuration
public class PedidosConfig {

    @Bean
    public NotificadorPedidos notificadorPedidos(JavaMailSender mailSender,
                                                 PropiedadesAutenticacion propiedades,
                                                 @Value("${app.pedidos.url-detalle}") String urlPedidos) {
        return new NotificadorPedidosSmtp(mailSender, propiedades.remitenteCorreo(), urlPedidos);
    }

    @Bean
    public RegistrarPedidoUseCase registrarPedidoUseCase(ArticuloRepository articuloRepository,
                                                         PedidoRepository pedidoRepository,
                                                         UsuarioRepository usuarioRepository,
                                                         NotificadorPedidos notificadorPedidos,
                                                         GestorTransacciones transacciones,
                                                         Clock reloj) {
        return new RegistrarPedidoUseCase(articuloRepository, pedidoRepository, usuarioRepository,
                notificadorPedidos, transacciones, reloj);
    }

    @Bean
    public ObtenerPedidoDeClienteUseCase obtenerPedidoDeClienteUseCase(PedidoRepository pedidoRepository) {
        return new ObtenerPedidoDeClienteUseCase(pedidoRepository);
    }

    // ---- F-07: gestión de estados del pedido ----

    @Bean
    public ListarPedidosUseCase listarPedidosUseCase(PedidoRepository pedidoRepository,
                                                     UsuarioRepository usuarioRepository) {
        return new ListarPedidosUseCase(pedidoRepository, usuarioRepository);
    }

    @Bean
    public ObtenerPedidoParaGestionUseCase obtenerPedidoParaGestionUseCase(PedidoRepository pedidoRepository,
                                                                           UsuarioRepository usuarioRepository) {
        return new ObtenerPedidoParaGestionUseCase(pedidoRepository, usuarioRepository);
    }

    @Bean
    public AvanzarEstadoPedidoUseCase avanzarEstadoPedidoUseCase(PedidoRepository pedidoRepository,
                                                                 GestorTransacciones transacciones, Clock reloj) {
        return new AvanzarEstadoPedidoUseCase(pedidoRepository, transacciones, reloj);
    }

    @Bean
    public CancelarPedidoUseCase cancelarPedidoUseCase(PedidoRepository pedidoRepository,
                                                       ArticuloRepository articuloRepository,
                                                       GestorTransacciones transacciones, Clock reloj) {
        return new CancelarPedidoUseCase(pedidoRepository, articuloRepository, transacciones, reloj);
    }

    @Bean
    public ListarPedidosDeClienteUseCase listarPedidosDeClienteUseCase(PedidoRepository pedidoRepository) {
        return new ListarPedidosDeClienteUseCase(pedidoRepository);
    }
}
