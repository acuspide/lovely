package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioTest {

    @Test
    void dosUsuariosConElMismoIdDebenSerIguales() {
        // Arrange
        Usuario usuario1 = new Usuario(1L, "Ana", new Email("ana@correo.com"), "hash1", RolUsuario.CLIENTE);
        Usuario usuario2 = new Usuario(1L, "Otro nombre", new Email("otro@correo.com"), "hash2", RolUsuario.ASESORA_VENTAS);

        // Act y Assert
        assertEquals(usuario1, usuario2);
    }

    @Test
    void unUsuarioNuevoDebeQuedarActivo() {
        // Arrange y Act
        Usuario usuario = new Usuario(1L, "Ana", new Email("ana@correo.com"), "hash", RolUsuario.CLIENTE);

        // Assert
        assertTrue(usuario.isActivo());
    }

    @Test
    void desactivarCambiaElEstadoDelUsuario() {
        // Arrange
        Usuario usuario = new Usuario(1L, "Ana", new Email("ana@correo.com"), "hash", RolUsuario.CLIENTE);

        // Act
        usuario.desactivar();

        // Assert
        assertFalse(usuario.isActivo());
    }

    @Test
    void activarReviveUnUsuarioDesactivado() {
        // Arrange
        Usuario usuario = new Usuario(1L, "Ana", new Email("ana@correo.com"), "hash", RolUsuario.CLIENTE);
        usuario.desactivar();

        // Act
        usuario.activar();

        // Assert
        assertTrue(usuario.isActivo());
    }

    @Test
    void conIdDevuelveUnaCopiaConElNuevoIdYElMismoEstado() {
        // Arrange
        Usuario usuario = new Usuario(0L, "Ana", new Email("ana@correo.com"), "hash", RolUsuario.CLIENTE);
        usuario.desactivar();

        // Act
        Usuario conId = usuario.conId(42L);

        // Assert
        assertEquals(42L, conId.getId());
        assertFalse(conId.isActivo());
    }

    @Test
    void noDebeCrearUsuarioSinNombre() {
        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            new Usuario(1L, " ", new Email("ana@correo.com"), "hash", RolUsuario.CLIENTE);
        });
    }

    @Test
    void noDebeCrearUsuarioSinRol() {
        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            new Usuario(1L, "Ana", new Email("ana@correo.com"), "hash", null);
        });
    }
}