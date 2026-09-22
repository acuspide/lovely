package com.example.ecommerce.infrastructure.config;

import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.application.TokenProvider;
import com.example.ecommerce.application.usecase.AutenticarUsuarioUseCase;
import com.example.ecommerce.application.usecase.CambiarEstadoUsuarioUseCase;
import com.example.ecommerce.application.usecase.IniciarSesionUseCase;
import com.example.ecommerce.application.usecase.ListarUsuariosUseCase;
import com.example.ecommerce.application.usecase.RegistrarUsuarioUseCase;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.infrastructure.security.BCryptPasswordHasher;
import com.example.ecommerce.infrastructure.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cablea como beans de Spring las piezas de dominio/aplicación (casos de uso
 * y puertos), que a propósito son clases planas de Java sin anotaciones de
 * framework. Demuestra DIP: esta clase es la única que conoce tanto los
 * puertos como sus implementaciones concretas (BCryptPasswordHasher,
 * JwtTokenProvider); todo lo demás depende solo de las interfaces.
 *
 * UsuarioRepository NO se declara aquí: su implementación de producción
 * (UsuarioRepositoryJpaAdapter) ya es un @Repository de Spring y se inyecta
 * directamente donde se necesite.
 */
@Configuration
public class AppConfig {

    @Bean
    public PasswordHasher passwordHasher() {
        return new BCryptPasswordHasher();
    }

    @Bean
    public TokenProvider tokenProvider(@Value("${app.jwt.secret}") String secreto,
                                        @Value("${app.jwt.expiracion-minutos}") long minutosDeExpiracion) {
        return new JwtTokenProvider(secreto, minutosDeExpiracion);
    }

    @Bean
    public RegistrarUsuarioUseCase registrarUsuarioUseCase(UsuarioRepository usuarioRepository,
                                                             PasswordHasher passwordHasher) {
        return new RegistrarUsuarioUseCase(usuarioRepository, passwordHasher);
    }

    @Bean
    public AutenticarUsuarioUseCase autenticarUsuarioUseCase(UsuarioRepository usuarioRepository,
                                                               PasswordHasher passwordHasher) {
        return new AutenticarUsuarioUseCase(usuarioRepository, passwordHasher);
    }

    @Bean
    public IniciarSesionUseCase iniciarSesionUseCase(AutenticarUsuarioUseCase autenticarUsuarioUseCase,
                                                       TokenProvider tokenProvider) {
        return new IniciarSesionUseCase(autenticarUsuarioUseCase, tokenProvider);
    }

    @Bean
    public ListarUsuariosUseCase listarUsuariosUseCase(UsuarioRepository usuarioRepository) {
        return new ListarUsuariosUseCase(usuarioRepository);
    }

    @Bean
    public CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase(UsuarioRepository usuarioRepository) {
        return new CambiarEstadoUsuarioUseCase(usuarioRepository);
    }
}
