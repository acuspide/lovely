package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Sesion;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.ContrasenaDebilException;
import com.example.ecommerce.domain.exception.CredencialesInvalidasException;
import com.example.ecommerce.domain.exception.EnlaceRecuperacionInvalidoException;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.infrastructure.security.GeneradorTokenSeguroSha256;
import com.example.ecommerce.support.EntornoAutenticacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.example.ecommerce.support.EntornoAutenticacion.CONTRASENA_VALIDA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF-01.8: recuperación de contraseña por correo con enlace de un solo uso. */
class RecuperacionContrasenaTest {

    private static final String CORREO = "ana@correo.com";
    private static final String NUEVA = "NuevaClave2026*";

    private EntornoAutenticacion entorno;
    private SolicitarRecuperacionContrasenaUseCase solicitar;
    private RestablecerContrasenaUseCase restablecer;
    private AutenticarUsuarioUseCase autenticar;

    @BeforeEach
    void preparar() {
        entorno = new EntornoAutenticacion();
        var generador = new GeneradorTokenSeguroSha256();
        solicitar = new SolicitarRecuperacionContrasenaUseCase(entorno.usuarios, entorno.tokensRecuperacion,
                generador, entorno.correo, entorno.transacciones, entorno.reloj);
        restablecer = new RestablecerContrasenaUseCase(entorno.usuarios, entorno.tokensRecuperacion,
                entorno.sesiones, generador, entorno.passwordHasher, entorno.transacciones, entorno.reloj);
        autenticar = new AutenticarUsuarioUseCase(entorno.usuarios, entorno.passwordHasher, entorno.transacciones, entorno.reloj);
    }

    private String solicitarEnlace() {
        solicitar.ejecutar(CORREO);
        return entorno.correo.ultimoToken().orElseThrow();
    }

    @Test
    void recuperacionCompletaPermiteEntrarConLaNuevaContrasena() {
        // Arrange
        entorno.registrarCliente(CORREO);
        String token = solicitarEnlace();

        // Act
        restablecer.ejecutar(token, NUEVA);

        // Assert
        assertDoesNotThrow(() -> autenticar.ejecutar(CORREO, NUEVA));
        assertThrows(CredencialesInvalidasException.class, () -> autenticar.ejecutar(CORREO, CONTRASENA_VALIDA));
    }

    @Test
    void elEnlaceYaUsadoNoSirveDeNuevo() {
        // Arrange
        entorno.registrarCliente(CORREO);
        String token = solicitarEnlace();
        restablecer.ejecutar(token, NUEVA);

        // Act y Assert
        assertThrows(EnlaceRecuperacionInvalidoException.class, () -> restablecer.ejecutar(token, "OtraClave2026*"));
    }

    @Test
    void elEnlaceVencidoNoSirve() {
        // Arrange
        entorno.registrarCliente(CORREO);
        String token = solicitarEnlace();
        entorno.reloj.avanzar(Duration.ofMinutes(31));

        // Act y Assert
        assertThrows(EnlaceRecuperacionInvalidoException.class, () -> restablecer.ejecutar(token, NUEVA));
    }

    @Test
    void pedirUnEnlaceNuevoAnulaElAnterior() {
        // Arrange
        entorno.registrarCliente(CORREO);
        String primero = solicitarEnlace();
        String segundo = solicitarEnlace();

        // Act y Assert
        assertThrows(EnlaceRecuperacionInvalidoException.class, () -> restablecer.ejecutar(primero, NUEVA));
        assertDoesNotThrow(() -> restablecer.ejecutar(segundo, NUEVA));
    }

    @Test
    void unaContrasenaDebilNoConsumeElEnlace() {
        // Arrange
        entorno.registrarCliente(CORREO);
        String token = solicitarEnlace();

        // Act
        assertThrows(ContrasenaDebilException.class, () -> restablecer.ejecutar(token, "debil"));

        // Assert
        assertDoesNotThrow(() -> restablecer.ejecutar(token, NUEVA));
    }

    @Test
    void unCorreoNoRegistradoNoRecibeEnlaceNiProduceError() {
        // Act
        assertDoesNotThrow(() -> solicitar.ejecutar("nadie@correo.com"));

        // Assert
        assertTrue(entorno.correo.enviados().isEmpty());
    }

    @Test
    void unaCuentaDesactivadaNoRecibeEnlace() {
        // Arrange
        Usuario asesora = entorno.crearPersonal(CORREO, RolUsuario.ASESORA_VENTAS);
        asesora.desactivar();
        entorno.usuarios.guardar(asesora);

        // Act
        solicitar.ejecutar(CORREO);

        // Assert
        assertTrue(entorno.correo.enviados().isEmpty());
    }

    @Test
    void elCorreoNoLlevaLaContrasenaYElTokenNoSeGuardaEnClaro() {
        // Arrange
        entorno.registrarCliente(CORREO);

        // Act
        String token = solicitarEnlace();

        // Assert
        assertEquals(CORREO, entorno.correo.enviados().getFirst().destinataria());
        assertTrue(entorno.tokensRecuperacion.buscarPorHuella(token).isEmpty());
    }

    @Test
    void restablecerCierraLasSesionesAbiertasYQuitaElBloqueo() {
        // Arrange
        Usuario cliente = entorno.registrarCliente(CORREO);
        entorno.sesiones.guardar(Sesion.iniciar("sesion-vieja", cliente.getId(), entorno.reloj.instant()));
        for (int i = 0; i < 5; i++) {
            assertThrows(RuntimeException.class, () -> autenticar.ejecutar(CORREO, "ClaveErrada1"));
        }
        String token = solicitarEnlace();

        // Act
        restablecer.ejecutar(token, NUEVA);

        // Assert
        assertTrue(entorno.sesiones.abiertasDe(cliente.getId()).isEmpty());
        assertDoesNotThrow(() -> autenticar.ejecutar(CORREO, NUEVA));
    }
}
