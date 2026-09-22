package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.infrastructure.persistence.UsuarioRepositoryEnMemoria;
import com.example.ecommerce.infrastructure.security.BCryptPasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AutenticarUsuarioUseCaseTest {

    @Test
    void debeAutenticarUnUsuarioConCredencialesCorrectas() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        PasswordHasher passwordHasher = new BCryptPasswordHasher();
        new RegistrarUsuarioUseCase(repository, passwordHasher)
                .ejecutar("Ana Pérez", "ana@correo.com", "clave123", RolUsuario.CLIENTE);
        AutenticarUsuarioUseCase useCase = new AutenticarUsuarioUseCase(repository, passwordHasher);

        // Act
        Usuario usuario = useCase.ejecutar("ana@correo.com", "clave123");

        // Assert
        assertEquals("ana@correo.com", usuario.getEmail().getValor());
    }

    @Test
    void noDebeAutenticarConContrasenaIncorrecta() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        PasswordHasher passwordHasher = new BCryptPasswordHasher();
        new RegistrarUsuarioUseCase(repository, passwordHasher)
                .ejecutar("Ana", "ana@correo.com", "clave123", RolUsuario.CLIENTE);
        AutenticarUsuarioUseCase useCase = new AutenticarUsuarioUseCase(repository, passwordHasher);

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            useCase.ejecutar("ana@correo.com", "claveIncorrecta");
        });
    }

    @Test
    void noDebeAutenticarUnCorreoNoRegistrado() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        PasswordHasher passwordHasher = new BCryptPasswordHasher();
        AutenticarUsuarioUseCase useCase = new AutenticarUsuarioUseCase(repository, passwordHasher);

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            useCase.ejecutar("noexiste@correo.com", "clave123");
        });
    }

    @Test
    void noDebeAutenticarUnUsuarioDesactivado() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        PasswordHasher passwordHasher = new BCryptPasswordHasher();
        Usuario usuario = new RegistrarUsuarioUseCase(repository, passwordHasher)
                .ejecutar("Ana", "ana@correo.com", "clave123", RolUsuario.CLIENTE);
        usuario.desactivar();
        repository.guardar(usuario);
        AutenticarUsuarioUseCase useCase = new AutenticarUsuarioUseCase(repository, passwordHasher);

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            useCase.ejecutar("ana@correo.com", "clave123");
        });
    }
}
