package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.infrastructure.persistence.UsuarioRepositoryEnMemoria;
import com.example.ecommerce.infrastructure.security.PasswordHasherSha256;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegistrarUsuarioUseCaseTest {

    @Test
    void debeRegistrarUnUsuarioNuevoConCorreoUnico() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        PasswordHasher passwordHasher = new PasswordHasherSha256();
        RegistrarUsuarioUseCase useCase = new RegistrarUsuarioUseCase(repository, passwordHasher);

        // Act
        Usuario usuario = useCase.ejecutar(1L, "Ana Pérez", "ana@correo.com", "clave123", RolUsuario.CLIENTE);

        // Assert
        assertEquals(RolUsuario.CLIENTE, usuario.getRol());
        assertTrue(repository.existePorEmail(new Email("ana@correo.com")));
    }

    @Test
    void noDebePermitirRegistrarDosUsuariosConElMismoCorreo() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        PasswordHasher passwordHasher = new PasswordHasherSha256();
        RegistrarUsuarioUseCase useCase = new RegistrarUsuarioUseCase(repository, passwordHasher);
        useCase.ejecutar(1L, "Ana", "ana@correo.com", "clave123", RolUsuario.CLIENTE);

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            useCase.ejecutar(2L, "Otra Ana", "ana@correo.com", "otraClave", RolUsuario.VENDEDOR);
        });
    }

    @Test
    void debeAsignarRolClientePorDefectoSiNoSeIndica() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        PasswordHasher passwordHasher = new PasswordHasherSha256();
        RegistrarUsuarioUseCase useCase = new RegistrarUsuarioUseCase(repository, passwordHasher);

        // Act
        Usuario usuario = useCase.ejecutar(1L, "Ana", "ana@correo.com", "clave123", null);

        // Assert
        assertEquals(RolUsuario.CLIENTE, usuario.getRol());
    }

    @Test
    void debeAlmacenarLaContrasenaComoHashYNoEnTextoPlano() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        PasswordHasher passwordHasher = new PasswordHasherSha256();
        RegistrarUsuarioUseCase useCase = new RegistrarUsuarioUseCase(repository, passwordHasher);

        // Act
        Usuario usuario = useCase.ejecutar(1L, "Ana", "ana@correo.com", "clave123", RolUsuario.CLIENTE);

        // Assert
        assertNotEquals("clave123", usuario.getContrasenaHash());
    }
}
