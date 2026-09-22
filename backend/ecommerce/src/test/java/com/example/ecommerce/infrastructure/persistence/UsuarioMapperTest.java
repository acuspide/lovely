package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.infrastructure.persistence.jpa.UsuarioJpaEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class UsuarioMapperTest {

    @Test
    void aEntidadDejaElIdEnNullCuandoElUsuarioAunNoSePersiste() {
        // Arrange
        Usuario usuario = new Usuario(0L, "Ana", new Email("ana@correo.com"), "hash", RolUsuario.CLIENTE);

        // Act
        UsuarioJpaEntity entidad = UsuarioMapper.aEntidad(usuario);

        // Assert
        assertNull(entidad.getId());
        assertEquals("ana@correo.com", entidad.getEmail());
        assertEquals(UsuarioJpaEntity.RolJpa.CLIENTE, entidad.getRol());
    }

    @Test
    void aDominioReconstruyeElUsuarioConSuEstadoDeActivacion() {
        // Arrange
        UsuarioJpaEntity entidad = new UsuarioJpaEntity(
                5L, "Ana", "ana@correo.com", "hash", UsuarioJpaEntity.RolJpa.ADMINISTRADORA, false);

        // Act
        Usuario usuario = UsuarioMapper.aDominio(entidad);

        // Assert
        assertEquals(5L, usuario.getId());
        assertEquals(RolUsuario.ADMINISTRADORA, usuario.getRol());
        assertFalse(usuario.isActivo());
    }

    @Test
    void elViajeDeIdaYVueltaConservaLosDatos() {
        // Arrange
        Usuario original = new Usuario(9L, "Ana", new Email("ana@correo.com"), "hash", RolUsuario.ENCARGADA_INVENTARIO);

        // Act
        Usuario reconstruido = UsuarioMapper.aDominio(UsuarioMapper.aEntidad(original));

        // Assert
        assertEquals(original.getId(), reconstruido.getId());
        assertEquals(original.getEmail(), reconstruido.getEmail());
        assertEquals(original.getRol(), reconstruido.getRol());
        assertEquals(original.isActivo(), reconstruido.isActivo());
    }
}
