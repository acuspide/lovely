package com.example.ecommerce.infrastructure.security;

import com.example.ecommerce.application.TokenProvider;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.TokenInvalidoException;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTest {

    private static final String SECRETO_DE_PRUEBA =
            "secreto-de-prueba-suficientemente-largo-para-hmac-sha256-0123456789";

    @Test
    void debeGenerarUnTokenYValidarloDevolviendoLosMismosDatos() {
        // Arrange
        JwtTokenProvider tokenProvider = new JwtTokenProvider(SECRETO_DE_PRUEBA, 30);
        Usuario usuario = new Usuario(7L, "Ana", new Email("ana@correo.com"), "hash", RolUsuario.ADMINISTRADORA);

        // Act
        String token = tokenProvider.generarToken(usuario);
        TokenProvider.DatosToken datos = tokenProvider.validar(token);

        // Assert
        assertEquals(7L, datos.idUsuario());
        assertEquals("ana@correo.com", datos.email());
        assertEquals("ADMINISTRADORA", datos.rol());
    }

    @Test
    void debeRechazarUnTokenConFirmaInvalida() {
        // Arrange
        JwtTokenProvider tokenProvider = new JwtTokenProvider(SECRETO_DE_PRUEBA, 30);
        String tokenManipulado = "esto.no.es-un-jwt-valido";

        // Act y Assert
        assertThrows(TokenInvalidoException.class, () -> tokenProvider.validar(tokenManipulado));
    }

    @Test
    void debeRechazarUnTokenYaExpirado() throws InterruptedException {
        // Arrange: expiración casi inmediata para forzar el vencimiento.
        JwtTokenProvider tokenProvider = new JwtTokenProvider(SECRETO_DE_PRUEBA, 0);
        Usuario usuario = new Usuario(1L, "Ana", new Email("ana@correo.com"), "hash", RolUsuario.CLIENTE);
        String token = tokenProvider.generarToken(usuario);

        Thread.sleep(50);

        // Act y Assert
        assertThrows(TokenInvalidoException.class, () -> tokenProvider.validar(token));
    }
}
