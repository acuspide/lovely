package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.CuentaNoEsPersonalException;
import com.example.ecommerce.domain.exception.HabeasDataNoAceptadoException;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.exception.RolNoPermitidoParaPersonalException;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.domain.valueobject.Telefono;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

    private static final Instant AHORA = Instant.parse("2026-10-14T15:00:00Z");

    private static Usuario cliente() {
        return Usuario.registrarCliente("Ana", new Email("ana@correo.com"), new Telefono("3001234567"), "hash", AHORA);
    }

    private static Usuario personal(RolUsuario rol) {
        return Usuario.crearPersonal("Laura", new Email("laura@correo.com"), null, "hash", rol);
    }

    // --- RF-01.1: registro de clientas -----------------------------------

    @Test
    void unaClienteRegistradaQuedaActivaConRolClienteYConstanciaDeHabeasData() {
        // Act
        Usuario usuario = cliente();

        // Assert
        assertEquals(RolUsuario.CLIENTE, usuario.getRol());
        assertTrue(usuario.isActivo());
        assertEquals(AHORA, usuario.getHabeasDataAceptadoEn().orElseThrow());
    }

    @Test
    void noSePuedeRegistrarUnaClienteSinAceptarHabeasData() {
        // Act y Assert
        assertThrows(HabeasDataNoAceptadoException.class, () -> Usuario.registrarCliente(
                "Ana", new Email("ana@correo.com"), new Telefono("3001234567"), "hash", null));
    }

    @Test
    void noSePuedeRegistrarUnaClienteSinTelefono() {
        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> Usuario.registrarCliente(
                "Ana", new Email("ana@correo.com"), null, "hash", AHORA));
    }

    @Test
    void noSePuedeCrearUnaCuentaSinNombre() {
        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> Usuario.registrarCliente(
                " ", new Email("ana@correo.com"), new Telefono("3001234567"), "hash", AHORA));
    }

    // --- RF-01.2: cuentas del personal ------------------------------------

    @Test
    void elPersonalNoPuedeCrearseConRolCliente() {
        // Act y Assert
        assertThrows(RolNoPermitidoParaPersonalException.class, () -> personal(RolUsuario.CLIENTE));
    }

    @Test
    void laEdicionDeDatosDePersonalNoAplicaAUnaCliente() {
        // Arrange
        Usuario usuario = cliente();

        // Act y Assert
        assertThrows(CuentaNoEsPersonalException.class, () -> usuario.actualizarDatosDePersonal(
                "Ana", new Email("ana@correo.com"), null, RolUsuario.ASESORA_VENTAS));
    }

    @Test
    void desactivarYActivarCambianElEstadoDeLaCuenta() {
        // Arrange
        Usuario usuario = personal(RolUsuario.ASESORA_VENTAS);

        // Act y Assert
        usuario.desactivar();
        assertFalse(usuario.isActivo());
        usuario.activar();
        assertTrue(usuario.isActivo());
    }

    @Test
    void unaAdministradoraDesactivadaNoCuentaComoAdministradoraActiva() {
        // Arrange
        Usuario administradora = personal(RolUsuario.ADMINISTRADORA);

        // Act
        administradora.desactivar();

        // Assert
        assertFalse(administradora.esAdministradoraActiva());
    }

    // --- RF-01.4: bloqueo por intentos fallidos ---------------------------

    @Test
    void cuatroIntentosFallidosNoBloqueanLaCuenta() {
        // Arrange
        Usuario usuario = cliente();

        // Act
        for (int i = 0; i < 4; i++) {
            usuario.registrarIntentoFallido(AHORA);
        }

        // Assert
        assertFalse(usuario.estaBloqueada(AHORA));
    }

    @Test
    void elQuintoIntentoFallidoBloqueaLaCuentaQuinceMinutos() {
        // Arrange
        Usuario usuario = cliente();

        // Act
        for (int i = 0; i < 5; i++) {
            usuario.registrarIntentoFallido(AHORA);
        }

        // Assert
        assertTrue(usuario.estaBloqueada(AHORA));
        assertEquals(15, usuario.minutosRestantesDeBloqueo(AHORA));
        assertTrue(usuario.estaBloqueada(AHORA.plus(Duration.ofMinutes(14).plusSeconds(59))));
        assertFalse(usuario.estaBloqueada(AHORA.plus(Duration.ofMinutes(15))));
    }

    @Test
    void trasVencerElBloqueoElConteoDeIntentosEmpiezaDeNuevo() {
        // Arrange
        Usuario usuario = cliente();
        for (int i = 0; i < 5; i++) {
            usuario.registrarIntentoFallido(AHORA);
        }
        Instant despuesDelBloqueo = AHORA.plus(Duration.ofMinutes(16));

        // Act
        usuario.registrarIntentoFallido(despuesDelBloqueo);

        // Assert
        assertEquals(1, usuario.getIntentosFallidos());
        assertFalse(usuario.estaBloqueada(despuesDelBloqueo));
    }

    @Test
    void unIngresoExitosoDevuelveElContadorACero() {
        // Arrange
        Usuario usuario = cliente();
        usuario.registrarIntentoFallido(AHORA);
        usuario.registrarIntentoFallido(AHORA);

        // Act
        usuario.registrarIngresoExitoso();

        // Assert
        assertEquals(0, usuario.getIntentosFallidos());
    }

    @Test
    void cambiarLaContrasenaQuitaElBloqueo() {
        // Arrange
        Usuario usuario = cliente();
        for (int i = 0; i < 5; i++) {
            usuario.registrarIntentoFallido(AHORA);
        }

        // Act
        usuario.cambiarContrasena("nuevo-hash");

        // Assert
        assertFalse(usuario.estaBloqueada(AHORA));
        assertEquals("nuevo-hash", usuario.getContrasenaHash());
    }

    // --- Identidad y RF-01.5 ----------------------------------------------

    @Test
    void conIdDevuelveUnaCopiaConElNuevoIdYElMismoEstado() {
        // Arrange
        Usuario usuario = cliente();
        usuario.desactivar();

        // Act
        Usuario conId = usuario.conId(42L);

        // Assert
        assertEquals(42L, conId.getId());
        assertFalse(conId.isActivo());
        assertEquals(usuario.getHabeasDataAceptadoEn(), conId.getHabeasDataAceptadoEn());
    }

    @Test
    void dosCuentasConElMismoIdSonIguales() {
        // Act y Assert
        assertEquals(cliente().conId(1L), personal(RolUsuario.ASESORA_VENTAS).conId(1L));
    }

    @Test
    void toStringNoIncluyeElHashDeLaContrasena() {
        // Act
        String texto = cliente().toString();

        // Assert
        assertFalse(texto.contains("hash"));
    }
}
