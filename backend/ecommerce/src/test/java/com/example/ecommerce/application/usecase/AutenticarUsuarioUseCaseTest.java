package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.GestorTransacciones;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.CredencialesInvalidasException;
import com.example.ecommerce.domain.exception.CuentaBloqueadaException;
import com.example.ecommerce.domain.exception.UsuarioInactivoException;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.support.EntornoAutenticacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.function.Supplier;

import static com.example.ecommerce.support.EntornoAutenticacion.CONTRASENA_VALIDA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/** RF-01.3 (credenciales) y RF-01.4 (bloqueo por intentos fallidos). */
class AutenticarUsuarioUseCaseTest {

    private static final String CORREO = "ana@correo.com";

    private EntornoAutenticacion entorno;
    private AutenticarUsuarioUseCase useCase;

    @BeforeEach
    void preparar() {
        entorno = new EntornoAutenticacion();
        useCase = new AutenticarUsuarioUseCase(entorno.usuarios, entorno.passwordHasher, entorno.transacciones, entorno.reloj);
    }

    private void fallarIntentos(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            try {
                useCase.ejecutar(CORREO, "ClaveErrada1");
            } catch (CredencialesInvalidasException | CuentaBloqueadaException esperada) {
                // Cada intento fallido es parte del escenario.
            }
        }
    }

    // --- RF-01.3 -----------------------------------------------------------

    @Test
    void autenticaConCredencialesCorrectas() {
        // Arrange
        entorno.registrarCliente(CORREO);

        // Act
        Usuario usuario = useCase.ejecutar(CORREO, CONTRASENA_VALIDA);

        // Assert
        assertEquals(CORREO, usuario.getEmail().getValor());
    }

    @Test
    void contrasenaErradaYCorreoDesconocidoDanElMismoMensajeGenerico() {
        // Arrange
        entorno.registrarCliente(CORREO);

        // Act
        var contrasenaErrada = assertThrows(CredencialesInvalidasException.class,
                () -> useCase.ejecutar(CORREO, "ClaveErrada1"));
        var correoDesconocido = assertThrows(CredencialesInvalidasException.class,
                () -> useCase.ejecutar("nadie@correo.com", CONTRASENA_VALIDA));
        var correoMalFormado = assertThrows(CredencialesInvalidasException.class,
                () -> useCase.ejecutar("no-es-un-correo", CONTRASENA_VALIDA));

        // Assert
        assertEquals("Correo o contraseña incorrectos.", contrasenaErrada.getMessage());
        assertEquals(contrasenaErrada.getMessage(), correoDesconocido.getMessage());
        assertEquals(contrasenaErrada.getMessage(), correoMalFormado.getMessage());
    }

    @Test
    void unaCuentaDesactivadaNoPuedeIniciarSesion() {
        // Arrange
        Usuario asesora = entorno.crearPersonal(CORREO, RolUsuario.ASESORA_VENTAS);
        asesora.desactivar();
        entorno.usuarios.guardar(asesora);

        // Act y Assert
        assertThrows(UsuarioInactivoException.class, () -> useCase.ejecutar(CORREO, CONTRASENA_VALIDA));
    }

    @Test
    void unaCuentaDesactivadaConContrasenaErradaRecibeElMensajeGenerico() {
        // Arrange: no se debe revelar que la cuenta existe y está desactivada.
        Usuario asesora = entorno.crearPersonal(CORREO, RolUsuario.ASESORA_VENTAS);
        asesora.desactivar();
        entorno.usuarios.guardar(asesora);

        // Act y Assert
        assertThrows(CredencialesInvalidasException.class, () -> useCase.ejecutar(CORREO, "ClaveErrada1"));
    }

    // --- RF-01.4 -----------------------------------------------------------

    @Test
    void elQuintoIntentoFallidoSeguidoBloqueaLaCuenta() {
        // Arrange
        entorno.registrarCliente(CORREO);
        fallarIntentos(4);

        // Act
        var bloqueo = assertThrows(CuentaBloqueadaException.class, () -> useCase.ejecutar(CORREO, "ClaveErrada1"));

        // Assert
        assertEquals(15, bloqueo.getMinutosRestantes());
    }

    @Test
    void unaCuentaBloqueadaRechazaInclusoLaContrasenaCorrecta() {
        // Arrange
        entorno.registrarCliente(CORREO);
        fallarIntentos(5);
        entorno.reloj.avanzar(Duration.ofMinutes(14));

        // Act y Assert
        assertThrows(CuentaBloqueadaException.class, () -> useCase.ejecutar(CORREO, CONTRASENA_VALIDA));
    }

    @Test
    void pasadosQuinceMinutosLaCuentaSeDesbloquea() {
        // Arrange
        entorno.registrarCliente(CORREO);
        fallarIntentos(5);
        entorno.reloj.avanzar(Duration.ofMinutes(15));

        // Act
        Usuario usuario = useCase.ejecutar(CORREO, CONTRASENA_VALIDA);

        // Assert
        assertEquals(0, usuario.getIntentosFallidos());
    }

    @Test
    void unIngresoExitosoReiniciaElContadorDeIntentos() {
        // Arrange
        Usuario cliente = entorno.registrarCliente(CORREO);
        fallarIntentos(4);

        // Act
        useCase.ejecutar(CORREO, CONTRASENA_VALIDA);
        fallarIntentos(4);

        // Assert: tras el ingreso exitoso, otros 4 fallos no bloquean.
        assertEquals(4, entorno.recargar(cliente).getIntentosFallidos());
        assertEquals(CORREO, useCase.ejecutar(CORREO, CONTRASENA_VALIDA).getEmail().getValor());
    }

    @Test
    void elIntentoFallidoSeConfirmaAntesDeRechazarElIngreso() {
        // Arrange: una transacción real revierte todo si la operación lanza
        // una excepción; este doble hace fallar la prueba si eso ocurre.
        GestorTransacciones transaccionQueNoDebeRevertirse = new GestorTransacciones() {
            @Override
            public <T> T enTransaccion(Supplier<T> operacion) {
                try {
                    return operacion.get();
                } catch (RuntimeException ex) {
                    return fail("El rechazo se lanzó dentro de la transacción y revertiría el intento contado.", ex);
                }
            }
        };
        useCase = new AutenticarUsuarioUseCase(
                entorno.usuarios, entorno.passwordHasher, transaccionQueNoDebeRevertirse, entorno.reloj);
        Usuario cliente = entorno.registrarCliente(CORREO);

        // Act
        assertThrows(CredencialesInvalidasException.class, () -> useCase.ejecutar(CORREO, "ClaveErrada1"));

        // Assert
        assertEquals(1, entorno.recargar(cliente).getIntentosFallidos());
    }
}
