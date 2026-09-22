package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.application.TokenProvider;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.infrastructure.persistence.UsuarioRepositoryEnMemoria;
import com.example.ecommerce.infrastructure.security.BCryptPasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IniciarSesionUseCaseTest {

    /** Test double del puerto TokenProvider: aísla este test de JJWT. */
    private static class TokenProviderFalso implements TokenProvider {
        @Override
        public String generarToken(Usuario usuario) {
            return "token-de-prueba-" + usuario.getId();
        }

        @Override
        public long minutosDeExpiracion() {
            return 30;
        }

        @Override
        public DatosToken validar(String token) {
            throw new UnsupportedOperationException("No usado en este test.");
        }
    }

    @Test
    void debeAutenticarYEmitirUnTokenConLosMinutosDeExpiracionConfigurados() {
        // Arrange
        UsuarioRepository repository = new UsuarioRepositoryEnMemoria();
        PasswordHasher passwordHasher = new BCryptPasswordHasher();
        new RegistrarUsuarioUseCase(repository, passwordHasher)
                .ejecutar("Ana", "ana@correo.com", "clave123", RolUsuario.CLIENTE);

        AutenticarUsuarioUseCase autenticarUsuarioUseCase = new AutenticarUsuarioUseCase(repository, passwordHasher);
        IniciarSesionUseCase useCase = new IniciarSesionUseCase(autenticarUsuarioUseCase, new TokenProviderFalso());

        // Act
        IniciarSesionUseCase.SesionIniciada sesion = useCase.ejecutar("ana@correo.com", "clave123");

        // Assert
        assertEquals("ana@correo.com", sesion.usuario().getEmail().getValor());
        assertEquals(30, sesion.minutosDeExpiracion());
        assertEquals("token-de-prueba-" + sesion.usuario().getId(), sesion.token());
    }
}
