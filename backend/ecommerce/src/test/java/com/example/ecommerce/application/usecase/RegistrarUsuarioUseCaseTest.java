package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.infrastructure.persistence.UsuarioRepositoryEnMemoria;
import com.example.ecommerce.infrastructure.security.BCryptPasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegistrarUsuarioUseCaseTest {

    @Test
    void debeRegistrarUnUsuarioNuevoConCorreoUnico() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        PasswordHasher passwordHasher = new BCryptPasswordHasher();
        RegistrarUsuarioUseCase useCase = new RegistrarUsuarioUseCase(repository, passwordHasher);

        // Act
        Usuario usuario = useCase.ejecutar("Ana Pérez", "ana@correo.com", "clave123", RolUsuario.CLIENTE);

        // Assert
        assertEquals(RolUsuario.CLIENTE, usuario.getRol());
        assertTrue(usuario.getId() > 0);
        assertTrue(repository.existePorEmail(new Email("ana@correo.com")));
    }

    @Test
    void noDebePermitirRegistrarDosUsuariosConElMismoCorreo() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        PasswordHasher passwordHasher = new BCryptPasswordHasher();
        RegistrarUsuarioUseCase useCase = new RegistrarUsuarioUseCase(repository, passwordHasher);
        useCase.ejecutar("Ana", "ana@correo.com", "clave123", RolUsuario.CLIENTE);

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            useCase.ejecutar("Otra Ana", "ana@correo.com", "otraClave", RolUsuario.ASESORA_VENTAS);
        });
    }

    @Test
    void debeAsignarRolClientePorDefectoSiNoSeIndica() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        PasswordHasher passwordHasher = new BCryptPasswordHasher();
        RegistrarUsuarioUseCase useCase = new RegistrarUsuarioUseCase(repository, passwordHasher);

        // Act
        Usuario usuario = useCase.ejecutar("Ana", "ana@correo.com", "clave123", null);

        // Assert
        assertEquals(RolUsuario.CLIENTE, usuario.getRol());
    }

    @Test
    void debeAlmacenarLaContrasenaComoHashYNoEnTextoPlano() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        PasswordHasher passwordHasher = new BCryptPasswordHasher();
        RegistrarUsuarioUseCase useCase = new RegistrarUsuarioUseCase(repository, passwordHasher);

        // Act
        Usuario usuario = useCase.ejecutar("Ana", "ana@correo.com", "clave123", RolUsuario.CLIENTE);

        // Assert
        assertNotEquals("clave123", usuario.getContrasenaHash());
    }

    @Test
    void debePermitirRegistrarCualquierRolInterno() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        PasswordHasher passwordHasher = new BCryptPasswordHasher();
        RegistrarUsuarioUseCase useCase = new RegistrarUsuarioUseCase(repository, passwordHasher);

        // Act
        Usuario asesora = useCase.ejecutar("Valentina", "valentina@correo.com", "clave123", RolUsuario.ASESORA_VENTAS);
        Usuario encargada = useCase.ejecutar("Laura", "laura@correo.com", "clave123", RolUsuario.ENCARGADA_INVENTARIO);
        Usuario admin = useCase.ejecutar("Yeraldin", "yeraldin@correo.com", "clave123", RolUsuario.ADMINISTRADORA);

        // Assert
        assertEquals(RolUsuario.ASESORA_VENTAS, asesora.getRol());
        assertEquals(RolUsuario.ENCARGADA_INVENTARIO, encargada.getRol());
        assertEquals(RolUsuario.ADMINISTRADORA, admin.getRol());
    }
}
