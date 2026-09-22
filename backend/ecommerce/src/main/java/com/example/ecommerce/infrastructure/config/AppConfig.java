package com.example.ecommerce.infrastructure.config;

import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.application.usecase.AutenticarUsuarioUseCase;
import com.example.ecommerce.application.usecase.RegistrarUsuarioUseCase;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.infrastructure.persistence.UsuarioRepositoryEnMemoria;
import com.example.ecommerce.infrastructure.security.PasswordHasherSha256;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Cablea como beans de Spring las piezas de dominio/aplicación que ya existen
 * en el proyecto (hexagonal, sin anotaciones), para que el controlador web
 * (F-01: Autenticación y gestión de rol) pueda usarlas por inyección.
 *
 * La implementación en memoria es intencional para este prototipo: se podrá
 * sustituir por un UsuarioRepository con JPA/MariaDB sin tocar el controlador.
 */
@Configuration
public class AppConfig {

    @Bean
    public UsuarioRepository usuarioRepository() {
        return new UsuarioRepositoryEnMemoria();
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new PasswordHasherSha256();
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

    /**
     * Generador simple de IDs correlativos mientras el repositorio es en memoria.
     */
    @Bean
    public AtomicLong usuarioIdGenerator() {
        return new AtomicLong(0);
    }
}
