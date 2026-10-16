package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.usecase.IniciarSesionUseCase.SesionIniciada;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.support.EntornoAutenticacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

import static com.example.ecommerce.support.EntornoAutenticacion.CONTRASENA_VALIDA;
import static com.example.ecommerce.support.EntornoAutenticacion.INACTIVIDAD_MAXIMA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF-01.3: cada uno de los cuatro roles inicia sesión. */
class IniciarSesionUseCaseTest {

    private EntornoAutenticacion entorno;
    private IniciarSesionUseCase useCase;

    @BeforeEach
    void preparar() {
        entorno = new EntornoAutenticacion();
        AutenticarUsuarioUseCase autenticar =
                new AutenticarUsuarioUseCase(entorno.usuarios, entorno.passwordHasher, entorno.transacciones, entorno.reloj);
        useCase = new IniciarSesionUseCase(autenticar, entorno.sesiones, entorno.tokenProvider,
                INACTIVIDAD_MAXIMA, entorno.reloj);
    }

    @ParameterizedTest
    @EnumSource(value = RolUsuario.class, names = {"ASESORA_VENTAS", "ENCARGADA_INVENTARIO", "ADMINISTRADORA"})
    void elPersonalIniciaSesionYRecibeSuRol(RolUsuario rol) {
        // Arrange
        entorno.crearPersonal("persona@lovelygirl.com", rol);

        // Act
        SesionIniciada sesion = useCase.ejecutar("persona@lovelygirl.com", CONTRASENA_VALIDA);

        // Assert
        assertEquals(rol, sesion.usuario().getRol());
    }

    @Test
    void laClienteIniciaSesionYSeAbreUnaSesionQueVenceEnTreintaMinutos() {
        // Arrange
        Usuario cliente = entorno.registrarCliente("ana@correo.com");

        // Act
        SesionIniciada sesion = useCase.ejecutar("ana@correo.com", CONTRASENA_VALIDA);

        // Assert
        assertEquals(RolUsuario.CLIENTE, sesion.usuario().getRol());
        assertEquals(INACTIVIDAD_MAXIMA, sesion.vigencia());
        assertEquals(1, entorno.sesiones.abiertasDe(cliente.getId()).size());
        assertTrue(sesion.token().endsWith("|" + cliente.getId()));
    }
}
