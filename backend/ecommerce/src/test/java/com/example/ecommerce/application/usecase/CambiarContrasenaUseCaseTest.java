package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Sesion;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.ContrasenaActualIncorrectaException;
import com.example.ecommerce.domain.exception.ContrasenaDebilException;
import com.example.ecommerce.domain.exception.ContrasenaRepetidaException;
import com.example.ecommerce.support.EntornoAutenticacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.ecommerce.support.EntornoAutenticacion.CONTRASENA_VALIDA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF-01.8: cambio de contraseña desde el perfil (IU-07). */
class CambiarContrasenaUseCaseTest {

    private EntornoAutenticacion entorno;
    private CambiarContrasenaUseCase useCase;
    private Usuario cliente;

    @BeforeEach
    void preparar() {
        entorno = new EntornoAutenticacion();
        useCase = new CambiarContrasenaUseCase(entorno.usuarios, entorno.sesiones, entorno.passwordHasher,
                entorno.transacciones, entorno.reloj);
        cliente = entorno.registrarCliente("ana@correo.com");
    }

    @Test
    void cambiaLaContrasenaYConservaSoloLaSesionActual() {
        // Arrange
        entorno.sesiones.guardar(Sesion.iniciar("actual", cliente.getId(), entorno.reloj.instant()));
        entorno.sesiones.guardar(Sesion.iniciar("otro-dispositivo", cliente.getId(), entorno.reloj.instant()));

        // Act
        useCase.ejecutar(cliente.getId(), "actual", CONTRASENA_VALIDA, "NuevaClave2026*");

        // Assert
        assertTrue(entorno.passwordHasher.verificar("NuevaClave2026*", entorno.recargar(cliente).getContrasenaHash()));
        assertEquals(1, entorno.sesiones.abiertasDe(cliente.getId()).size());
        assertEquals("actual", entorno.sesiones.abiertasDe(cliente.getId()).getFirst().getId());
    }

    @Test
    void rechazaElCambioSiLaContrasenaActualEstaMalEscrita() {
        // Act y Assert
        assertThrows(ContrasenaActualIncorrectaException.class,
                () -> useCase.ejecutar(cliente.getId(), "actual", "ClaveErrada1", "NuevaClave2026*"));
        assertTrue(entorno.passwordHasher.verificar(CONTRASENA_VALIDA, entorno.recargar(cliente).getContrasenaHash()));
    }

    @Test
    void laNuevaContrasenaDebeCumplirLasReglasDelRegistro() {
        // Act y Assert
        assertThrows(ContrasenaDebilException.class,
                () -> useCase.ejecutar(cliente.getId(), "actual", CONTRASENA_VALIDA, "corta1"));
    }

    @Test
    void laNuevaContrasenaDebeSerDistintaDeLaActual() {
        // Act y Assert
        assertThrows(ContrasenaRepetidaException.class,
                () -> useCase.ejecutar(cliente.getId(), "actual", CONTRASENA_VALIDA, CONTRASENA_VALIDA));
    }
}
