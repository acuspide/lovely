package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.domain.valueobject.Telefono;
import com.example.ecommerce.infrastructure.persistence.jpa.UsuarioJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioMapperTest {

    private static final Instant ACEPTACION = Instant.parse("2026-10-14T15:00:00Z");

    @Test
    void aEntidadDejaElIdEnNullCuandoElUsuarioAunNoSePersiste() {
        // Arrange
        Usuario usuario = Usuario.registrarCliente(
                "Ana", new Email("ana@correo.com"), new Telefono("3001234567"), "hash", ACEPTACION);

        // Act
        UsuarioJpaEntity entidad = UsuarioMapper.aEntidad(usuario);

        // Assert
        assertNull(entidad.getId());
        assertEquals("ana@correo.com", entidad.getEmail());
        assertEquals("3001234567", entidad.getTelefono());
        assertEquals(UsuarioJpaEntity.RolJpa.CLIENTE, entidad.getRol());
        assertEquals(LocalDateTime.of(2026, 10, 14, 15, 0), entidad.getHabeasDataAceptadoEn());
    }

    @Test
    void aDominioReconstruyeUnaCuentaAntiguaSinTelefono() {
        // Arrange
        UsuarioJpaEntity entidad = new UsuarioJpaEntity(5L, "Ana", "ana@correo.com", null, "hash",
                UsuarioJpaEntity.RolJpa.ADMINISTRADORA, false, 0, null, null);

        // Act
        Usuario usuario = UsuarioMapper.aDominio(entidad);

        // Assert
        assertEquals(5L, usuario.getId());
        assertEquals(RolUsuario.ADMINISTRADORA, usuario.getRol());
        assertFalse(usuario.isActivo());
        assertTrue(usuario.getTelefono().isEmpty());
    }

    @Test
    void elViajeDeIdaYVueltaConservaElBloqueoYLosIntentos() {
        // Arrange
        Usuario original = Usuario.crearPersonal(
                "Laura", new Email("laura@correo.com"), null, "hash", RolUsuario.ENCARGADA_INVENTARIO).conId(9L);
        for (int i = 0; i < 5; i++) {
            original.registrarIntentoFallido(ACEPTACION);
        }

        // Act
        Usuario reconstruido = UsuarioMapper.aDominio(UsuarioMapper.aEntidad(original));

        // Assert
        assertEquals(original.getId(), reconstruido.getId());
        assertEquals(original.getEmail(), reconstruido.getEmail());
        assertEquals(original.getRol(), reconstruido.getRol());
        assertEquals(5, reconstruido.getIntentosFallidos());
        assertEquals(original.getBloqueadoHasta(), reconstruido.getBloqueadoHasta());
        assertTrue(reconstruido.estaBloqueada(ACEPTACION));
    }
}
