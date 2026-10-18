package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.usecase.IniciarSesionUseCase.SesionIniciada;
import com.example.ecommerce.application.usecase.ValidarSesionUseCase.SesionActiva;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.TokenInvalidoException;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.support.EntornoAutenticacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.example.ecommerce.support.EntornoAutenticacion.CONTRASENA_VALIDA;
import static com.example.ecommerce.support.EntornoAutenticacion.INACTIVIDAD_MAXIMA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** RF-01.6 (vencimiento por inactividad y renovación) y RF-01.7 (cierre manual). */
class SesionUseCasesTest {

    private EntornoAutenticacion entorno;
    private IniciarSesionUseCase iniciarSesion;
    private ValidarSesionUseCase validarSesion;
    private CerrarSesionUseCase cerrarSesion;

    @BeforeEach
    void preparar() {
        entorno = new EntornoAutenticacion();
        AutenticarUsuarioUseCase autenticar =
                new AutenticarUsuarioUseCase(entorno.usuarios, entorno.passwordHasher, entorno.transacciones, entorno.reloj);
        iniciarSesion = new IniciarSesionUseCase(autenticar, entorno.sesiones, entorno.tokenProvider,
                INACTIVIDAD_MAXIMA, entorno.reloj);
        validarSesion = new ValidarSesionUseCase(entorno.tokenProvider, entorno.sesiones, entorno.usuarios,
                INACTIVIDAD_MAXIMA, entorno.reloj);
        cerrarSesion = new CerrarSesionUseCase(entorno.sesiones, entorno.reloj);
    }

    private SesionIniciada iniciarComoAsesora() {
        entorno.crearPersonal("asesora@lovelygirl.com", RolUsuario.ASESORA_VENTAS);
        return iniciarSesion.ejecutar("asesora@lovelygirl.com", CONTRASENA_VALIDA);
    }

    // --- RF-01.6 -----------------------------------------------------------

    @Test
    void unaSesionConActividadRecienteEsValidaYDevuelveElRolDeLaCuenta() {
        // Arrange
        SesionIniciada sesion = iniciarComoAsesora();
        entorno.reloj.avanzar(Duration.ofMinutes(10));

        // Act
        SesionActiva activa = validarSesion.ejecutar(sesion.token());

        // Assert
        assertEquals(RolUsuario.ASESORA_VENTAS, activa.rol());
    }

    @Test
    void laSesionVenceTrasTreintaMinutosSinActividad() {
        // Arrange
        SesionIniciada sesion = iniciarComoAsesora();
        entorno.reloj.avanzar(Duration.ofMinutes(30));

        // Act y Assert
        assertThrows(TokenInvalidoException.class, () -> validarSesion.ejecutar(sesion.token()));
    }

    @Test
    void cadaPeticionRenuevaLaSesionOtrosTreintaMinutos() {
        // Arrange
        SesionIniciada sesion = iniciarComoAsesora();
        entorno.reloj.avanzar(Duration.ofMinutes(28));

        // Act: "Seguir conectado" a dos minutos del vencimiento.
        SesionActiva renovada = validarSesion.ejecutar(sesion.token());
        entorno.reloj.avanzar(Duration.ofMinutes(20));

        // Assert: vuelve a tener 30 minutos; 48 minutos después del inicio
        // (pero solo 20 de inactividad) la sesión sigue vigente.
        assertEquals(INACTIVIDAD_MAXIMA, renovada.vigencia());
        assertEquals(INACTIVIDAD_MAXIMA, validarSesion.ejecutar(sesion.token()).vigencia());
    }

    @Test
    void elCambioDeRolSeAplicaSinVolverAIniciarSesion() {
        // Arrange
        SesionIniciada sesion = iniciarComoAsesora();
        Usuario cuenta = entorno.recargar(sesion.usuario());
        cuenta.actualizarDatosDePersonal(cuenta.getNombre(), cuenta.getEmail(), null,
                RolUsuario.ENCARGADA_INVENTARIO);
        entorno.usuarios.guardar(cuenta);

        // Act
        SesionActiva activa = validarSesion.ejecutar(sesion.token());

        // Assert
        assertEquals(RolUsuario.ENCARGADA_INVENTARIO, activa.rol());
    }

    @Test
    void laSesionDeUnaCuentaDesactivadaDejaDeSerValida() {
        // Arrange
        SesionIniciada sesion = iniciarComoAsesora();
        Usuario cuenta = entorno.recargar(sesion.usuario());
        cuenta.desactivar();
        entorno.usuarios.guardar(cuenta);

        // Act y Assert
        assertThrows(TokenInvalidoException.class, () -> validarSesion.ejecutar(sesion.token()));
    }

    // --- RF-01.7 -----------------------------------------------------------

    @Test
    void trasCerrarSesionElTokenYaNoDaAcceso() {
        // Arrange
        SesionIniciada sesion = iniciarComoAsesora();
        String idSesion = validarSesion.ejecutar(sesion.token()).idSesion();

        // Act
        cerrarSesion.ejecutar(idSesion);

        // Assert
        assertThrows(TokenInvalidoException.class, () -> validarSesion.ejecutar(sesion.token()));
    }

    @Test
    void unTokenMalFormadoEsRechazado() {
        // Act y Assert
        assertThrows(TokenInvalidoException.class, () -> validarSesion.ejecutar("token-inventado"));
    }
}
