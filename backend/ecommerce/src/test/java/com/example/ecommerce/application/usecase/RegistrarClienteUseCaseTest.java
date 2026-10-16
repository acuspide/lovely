package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.usecase.RegistrarClienteUseCase.SolicitudRegistro;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.ContrasenaDebilException;
import com.example.ecommerce.domain.exception.CorreoElectronicoDuplicadoException;
import com.example.ecommerce.domain.exception.HabeasDataNoAceptadoException;
import com.example.ecommerce.domain.exception.TelefonoInvalidoException;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.support.EntornoAutenticacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF-01.1: registro de clientas. */
class RegistrarClienteUseCaseTest {

    private EntornoAutenticacion entorno;
    private RegistrarClienteUseCase useCase;

    @BeforeEach
    void preparar() {
        entorno = new EntornoAutenticacion();
        useCase = new RegistrarClienteUseCase(entorno.usuarios, entorno.passwordHasher, entorno.reloj);
    }

    private static SolicitudRegistro solicitud(String email, String contrasena, boolean aceptaHabeasData) {
        return new SolicitudRegistro("Ana Pérez", email, "300 123 4567", contrasena, aceptaHabeasData);
    }

    @Test
    void registroExitosoCreaUnaClienteActivaConRolCliente() {
        // Act
        Usuario cliente = useCase.ejecutar(solicitud("ana@correo.com", "Lovely2026*", true));

        // Assert
        assertTrue(cliente.getId() > 0);
        assertEquals(RolUsuario.CLIENTE, cliente.getRol());
        assertTrue(cliente.isActivo());
        assertEquals("3001234567", cliente.getTelefono().orElseThrow().getValor());
        assertEquals(entorno.reloj.instant(), cliente.getHabeasDataAceptadoEn().orElseThrow());
        assertTrue(entorno.usuarios.existePorEmail(new Email("ana@correo.com")));
    }

    @Test
    void laContrasenaSeGuardaCifradaYNoEnTextoPlano() {
        // Act
        Usuario cliente = useCase.ejecutar(solicitud("ana@correo.com", "Lovely2026*", true));

        // Assert
        assertNotEquals("Lovely2026*", cliente.getContrasenaHash());
    }

    @Test
    void noPermiteRegistrarUnCorreoRepetido() {
        // Arrange
        useCase.ejecutar(solicitud("ana@correo.com", "Lovely2026*", true));

        // Act y Assert
        assertThrows(CorreoElectronicoDuplicadoException.class,
                () -> useCase.ejecutar(solicitud("ANA@correo.com", "OtraClave99*", true)));
    }

    @Test
    void noPermiteRegistrarseConUnaContrasenaDebil() {
        // Act y Assert
        assertThrows(ContrasenaDebilException.class,
                () -> useCase.ejecutar(solicitud("ana@correo.com", "soloLetras", true)));
        assertFalse(entorno.usuarios.existePorEmail(new Email("ana@correo.com")));
    }

    @Test
    void noPermiteRegistrarseSinAceptarHabeasData() {
        // Act y Assert
        assertThrows(HabeasDataNoAceptadoException.class,
                () -> useCase.ejecutar(solicitud("ana@correo.com", "Lovely2026*", false)));
        assertFalse(entorno.usuarios.existePorEmail(new Email("ana@correo.com")));
    }

    @Test
    void noPermiteRegistrarseConUnTelefonoInvalido() {
        // Act y Assert
        assertThrows(TelefonoInvalidoException.class, () -> useCase.ejecutar(
                new SolicitudRegistro("Ana", "ana@correo.com", "12345", "Lovely2026*", true)));
    }

    @Test
    void toStringDeLaSolicitudNoIncluyeLaContrasena() {
        // Act y Assert
        assertFalse(solicitud("ana@correo.com", "Lovely2026*", true).toString().contains("Lovely2026*"));
    }
}
