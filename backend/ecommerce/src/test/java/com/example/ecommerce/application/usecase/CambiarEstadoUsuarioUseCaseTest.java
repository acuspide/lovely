package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.UsuarioNoEncontradoException;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.infrastructure.persistence.UsuarioRepositoryEnMemoria;
import com.example.ecommerce.infrastructure.security.BCryptPasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CambiarEstadoUsuarioUseCaseTest {

    @Test
    void debeDesactivarUnUsuarioExistente() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        PasswordHasher passwordHasher = new BCryptPasswordHasher();
        Usuario usuario = new RegistrarUsuarioUseCase(repository, passwordHasher)
                .ejecutar("Ana", "ana@correo.com", "clave123", RolUsuario.CLIENTE);
        CambiarEstadoUsuarioUseCase useCase = new CambiarEstadoUsuarioUseCase(repository);

        // Act
        Usuario actualizado = useCase.ejecutar(usuario.getId(), false);

        // Assert
        assertFalse(actualizado.isActivo());
    }

    @Test
    void debeReactivarUnUsuarioDesactivado() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        PasswordHasher passwordHasher = new BCryptPasswordHasher();
        Usuario usuario = new RegistrarUsuarioUseCase(repository, passwordHasher)
                .ejecutar("Ana", "ana@correo.com", "clave123", RolUsuario.CLIENTE);
        CambiarEstadoUsuarioUseCase useCase = new CambiarEstadoUsuarioUseCase(repository);
        useCase.ejecutar(usuario.getId(), false);

        // Act
        Usuario actualizado = useCase.ejecutar(usuario.getId(), true);

        // Assert
        assertTrue(actualizado.isActivo());
    }

    @Test
    void debeFallarSiElUsuarioNoExiste() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        CambiarEstadoUsuarioUseCase useCase = new CambiarEstadoUsuarioUseCase(repository);

        // Act y Assert
        assertThrows(UsuarioNoEncontradoException.class, () -> useCase.ejecutar(999L, false));
    }
}
