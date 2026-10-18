package com.example.ecommerce.infrastructure.security;

import com.example.ecommerce.application.TokenProvider;
import com.example.ecommerce.domain.entity.Sesion;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.TokenInvalidoException;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.support.RelojControlable;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTest {

    private static final String SECRETO_DE_PRUEBA =
            "secreto-de-prueba-suficientemente-largo-para-hmac-sha256-0123456789";

    private final RelojControlable reloj = new RelojControlable(Instant.parse("2026-10-14T15:00:00Z"));
    private final JwtTokenProvider tokenProvider = new JwtTokenProvider(SECRETO_DE_PRUEBA, Duration.ofHours(12), reloj);

    private final Usuario usuario = Usuario.crearPersonal(
            "Ana", new Email("ana@correo.com"), null, "hash", RolUsuario.ADMINISTRADORA).conId(7L);
    private final Sesion sesion = Sesion.iniciar("sesion-123", 7L, reloj.instant());

    @Test
    void elTokenLlevaLaSesionYElUsuario() {
        // Act
        String token = tokenProvider.generarToken(usuario, sesion);
        TokenProvider.DatosToken datos = tokenProvider.leer(token);

        // Assert
        assertEquals("sesion-123", datos.idSesion());
        assertEquals(7L, datos.idUsuario());
    }

    @Test
    void rechazaUnTokenConFirmaInvalida() {
        // Arrange
        JwtTokenProvider otroEmisor = new JwtTokenProvider(
                "otro-secreto-distinto-tambien-largo-para-hmac-sha256-9876543210", Duration.ofHours(12), reloj);
        String tokenAjeno = otroEmisor.generarToken(usuario, sesion);

        // Act y Assert
        assertThrows(TokenInvalidoException.class, () -> tokenProvider.leer(tokenAjeno));
        assertThrows(TokenInvalidoException.class, () -> tokenProvider.leer("esto.no.es-un-jwt"));
    }

    @Test
    void rechazaUnTokenQueSuperoSuDuracionMaxima() {
        // Arrange
        String token = tokenProvider.generarToken(usuario, sesion);
        reloj.avanzar(Duration.ofHours(12).plusSeconds(1));

        // Act y Assert
        assertThrows(TokenInvalidoException.class, () -> tokenProvider.leer(token));
    }
}
