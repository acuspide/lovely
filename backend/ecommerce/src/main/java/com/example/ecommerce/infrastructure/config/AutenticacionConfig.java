package com.example.ecommerce.infrastructure.config;

import com.example.ecommerce.application.GeneradorTokenSeguro;
import com.example.ecommerce.application.GestorTransacciones;
import com.example.ecommerce.application.NotificadorCorreo;
import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.application.TokenProvider;
import com.example.ecommerce.application.usecase.AutenticarUsuarioUseCase;
import com.example.ecommerce.application.usecase.CambiarContrasenaUseCase;
import com.example.ecommerce.application.usecase.CambiarEstadoUsuarioUseCase;
import com.example.ecommerce.application.usecase.CerrarSesionUseCase;
import com.example.ecommerce.application.usecase.CrearCuentaPersonalUseCase;
import com.example.ecommerce.application.usecase.EditarCuentaPersonalUseCase;
import com.example.ecommerce.application.usecase.IniciarSesionUseCase;
import com.example.ecommerce.application.usecase.ListarPersonalUseCase;
import com.example.ecommerce.application.usecase.ObtenerPerfilUseCase;
import com.example.ecommerce.application.usecase.RegistrarClienteUseCase;
import com.example.ecommerce.application.usecase.RestablecerContrasenaUseCase;
import com.example.ecommerce.application.usecase.SolicitarRecuperacionContrasenaUseCase;
import com.example.ecommerce.application.usecase.ValidarSesionUseCase;
import com.example.ecommerce.domain.repository.SesionRepository;
import com.example.ecommerce.domain.repository.TokenRecuperacionRepository;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.service.ReglaAdministradoraActiva;
import com.example.ecommerce.infrastructure.correo.NotificadorCorreoSmtp;
import com.example.ecommerce.infrastructure.persistence.SpringGestorTransacciones;
import com.example.ecommerce.infrastructure.security.BCryptPasswordHasher;
import com.example.ecommerce.infrastructure.security.GeneradorTokenSeguroSha256;
import com.example.ecommerce.infrastructure.security.JwtTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

/**
 * Cablea las piezas de F-01 (autenticación y gestión de roles). Los casos de
 * uso y los puertos son clases planas de Java sin anotaciones de framework;
 * esta clase es la única que conoce a la vez los puertos y sus
 * implementaciones concretas (DIP).
 */
@Configuration
@EnableConfigurationProperties(PropiedadesAutenticacion.class)
public class AutenticacionConfig {

    // ---------------------------------------------------------------------
    // Adaptadores de los puertos
    // ---------------------------------------------------------------------

    @Bean
    public PasswordHasher passwordHasher(PropiedadesAutenticacion propiedades) {
        return new BCryptPasswordHasher(propiedades.bcryptCosto());
    }

    @Bean
    public TokenProvider tokenProvider(PropiedadesAutenticacion propiedades, Clock reloj) {
        return new JwtTokenProvider(propiedades.jwtSecreto(), propiedades.duracionMaximaToken(), reloj);
    }

    @Bean
    public GeneradorTokenSeguro generadorTokenSeguro() {
        return new GeneradorTokenSeguroSha256();
    }

    @Bean
    public NotificadorCorreo notificadorCorreo(JavaMailSender mailSender, PropiedadesAutenticacion propiedades) {
        return new NotificadorCorreoSmtp(
                mailSender, propiedades.remitenteCorreo(), propiedades.urlRestablecerContrasena());
    }

    @Bean
    public GestorTransacciones gestorTransacciones(PlatformTransactionManager transactionManager) {
        return new SpringGestorTransacciones(new TransactionTemplate(transactionManager));
    }

    @Bean
    public ReglaAdministradoraActiva reglaAdministradoraActiva(UsuarioRepository usuarioRepository) {
        return new ReglaAdministradoraActiva(usuarioRepository);
    }

    // ---------------------------------------------------------------------
    // Registro y gestión de cuentas (RF-01.1, RF-01.2)
    // ---------------------------------------------------------------------

    @Bean
    public RegistrarClienteUseCase registrarClienteUseCase(UsuarioRepository usuarioRepository,
                                                           PasswordHasher passwordHasher, Clock reloj) {
        return new RegistrarClienteUseCase(usuarioRepository, passwordHasher, reloj);
    }

    @Bean
    public CrearCuentaPersonalUseCase crearCuentaPersonalUseCase(UsuarioRepository usuarioRepository,
                                                                 PasswordHasher passwordHasher) {
        return new CrearCuentaPersonalUseCase(usuarioRepository, passwordHasher);
    }

    @Bean
    public EditarCuentaPersonalUseCase editarCuentaPersonalUseCase(UsuarioRepository usuarioRepository,
                                                                   ReglaAdministradoraActiva regla) {
        return new EditarCuentaPersonalUseCase(usuarioRepository, regla);
    }

    @Bean
    public CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase(UsuarioRepository usuarioRepository,
                                                                   SesionRepository sesionRepository,
                                                                   ReglaAdministradoraActiva regla,
                                                                   GestorTransacciones transacciones,
                                                                   Clock reloj) {
        return new CambiarEstadoUsuarioUseCase(usuarioRepository, sesionRepository, regla, transacciones, reloj);
    }

    @Bean
    public ListarPersonalUseCase listarPersonalUseCase(UsuarioRepository usuarioRepository) {
        return new ListarPersonalUseCase(usuarioRepository);
    }

    @Bean
    public ObtenerPerfilUseCase obtenerPerfilUseCase(UsuarioRepository usuarioRepository) {
        return new ObtenerPerfilUseCase(usuarioRepository);
    }

    // ---------------------------------------------------------------------
    // Sesión (RF-01.3, RF-01.4, RF-01.6, RF-01.7)
    // ---------------------------------------------------------------------

    @Bean
    public AutenticarUsuarioUseCase autenticarUsuarioUseCase(UsuarioRepository usuarioRepository,
                                                             PasswordHasher passwordHasher,
                                                             GestorTransacciones transacciones, Clock reloj) {
        return new AutenticarUsuarioUseCase(usuarioRepository, passwordHasher, transacciones, reloj);
    }

    @Bean
    public IniciarSesionUseCase iniciarSesionUseCase(AutenticarUsuarioUseCase autenticarUsuarioUseCase,
                                                     SesionRepository sesionRepository,
                                                     TokenProvider tokenProvider,
                                                     PropiedadesAutenticacion propiedades,
                                                     Clock reloj) {
        return new IniciarSesionUseCase(autenticarUsuarioUseCase, sesionRepository, tokenProvider,
                propiedades.inactividadMaxima(), reloj);
    }

    @Bean
    public ValidarSesionUseCase validarSesionUseCase(TokenProvider tokenProvider,
                                                     SesionRepository sesionRepository,
                                                     UsuarioRepository usuarioRepository,
                                                     PropiedadesAutenticacion propiedades,
                                                     Clock reloj) {
        return new ValidarSesionUseCase(tokenProvider, sesionRepository, usuarioRepository,
                propiedades.inactividadMaxima(), reloj);
    }

    @Bean
    public CerrarSesionUseCase cerrarSesionUseCase(SesionRepository sesionRepository, Clock reloj) {
        return new CerrarSesionUseCase(sesionRepository, reloj);
    }

    // ---------------------------------------------------------------------
    // Contraseña (RF-01.8)
    // ---------------------------------------------------------------------

    @Bean
    public SolicitarRecuperacionContrasenaUseCase solicitarRecuperacionContrasenaUseCase(
            UsuarioRepository usuarioRepository,
            TokenRecuperacionRepository tokenRepository,
            GeneradorTokenSeguro generadorToken,
            NotificadorCorreo notificadorCorreo,
            GestorTransacciones transacciones,
            Clock reloj) {
        return new SolicitarRecuperacionContrasenaUseCase(usuarioRepository, tokenRepository, generadorToken,
                notificadorCorreo, transacciones, reloj);
    }

    @Bean
    public RestablecerContrasenaUseCase restablecerContrasenaUseCase(UsuarioRepository usuarioRepository,
                                                                     TokenRecuperacionRepository tokenRepository,
                                                                     SesionRepository sesionRepository,
                                                                     GeneradorTokenSeguro generadorToken,
                                                                     PasswordHasher passwordHasher,
                                                                     GestorTransacciones transacciones,
                                                                     Clock reloj) {
        return new RestablecerContrasenaUseCase(usuarioRepository, tokenRepository, sesionRepository,
                generadorToken, passwordHasher, transacciones, reloj);
    }

    @Bean
    public CambiarContrasenaUseCase cambiarContrasenaUseCase(UsuarioRepository usuarioRepository,
                                                             SesionRepository sesionRepository,
                                                             PasswordHasher passwordHasher,
                                                             GestorTransacciones transacciones,
                                                             Clock reloj) {
        return new CambiarContrasenaUseCase(usuarioRepository, sesionRepository, passwordHasher,
                transacciones, reloj);
    }
}
