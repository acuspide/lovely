package com.example.ecommerce.infrastructure.config;

import com.example.ecommerce.application.usecase.RegistrarUsuarioUseCase;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Carga tres usuarios de demostración (uno por cada rol de RolUsuario) para
 * que el login y la sustentación se puedan probar sin pasar primero por
 * "Crear cuenta". Solo vive en este prototipo; no aplica en producción.
 */
@Component
public class DemoDataLoader implements CommandLineRunner {

    public static final String PASSWORD_DEMO = "Glow2026*";

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final AtomicLong usuarioIdGenerator;

    public DemoDataLoader(RegistrarUsuarioUseCase registrarUsuarioUseCase,
                           AtomicLong usuarioIdGenerator) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.usuarioIdGenerator = usuarioIdGenerator;
    }

    @Override
    public void run(String... args) {
        registrarUsuarioUseCase.ejecutar(
                usuarioIdGenerator.incrementAndGet(),
                "Camila Ríos",
                "cliente@glowshop.com",
                PASSWORD_DEMO,
                RolUsuario.CLIENTE);

        registrarUsuarioUseCase.ejecutar(
                usuarioIdGenerator.incrementAndGet(),
                "Boutique Aurora",
                "boutique@glowshop.com",
                PASSWORD_DEMO,
                RolUsuario.VENDEDOR);

        registrarUsuarioUseCase.ejecutar(
                usuarioIdGenerator.incrementAndGet(),
                "Admin Glow Shop",
                "admin@glowshop.com",
                PASSWORD_DEMO,
                RolUsuario.ADMINISTRADOR);
    }
}
