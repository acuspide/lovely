package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.NombreArticulo;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.infrastructure.persistence.jpa.ArticuloJpaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ArticuloMapperTest {

    @Test
    void aEntidadDejaElIdEnNullCuandoElArticuloAunNoSePersiste() {
        // Arrange
        Articulo articulo = new Articulo(0L, new NombreArticulo("Labial"), "Descripción",
                new Precio(new BigDecimal("20000")), CategoriaArticulo.MAQUILLAJE,
                "Maybelline", "https://ejemplo.com/labial.jpg", 10);

        // Act
        ArticuloJpaEntity entidad = ArticuloMapper.aEntidad(articulo);

        // Assert
        assertNull(entidad.getId());
        assertEquals("Labial", entidad.getNombre());
        assertEquals(ArticuloJpaEntity.CategoriaJpa.MAQUILLAJE, entidad.getCategoria());
    }

    @Test
    void elViajeDeIdaYVueltaConservaLosDatos() {
        // Arrange
        Articulo original = new Articulo(9L, new NombreArticulo("Shampoo"), "Descripción",
                new Precio(new BigDecimal("25000")), CategoriaArticulo.CAPILAR,
                null, "https://ejemplo.com/shampoo.jpg", 7);

        // Act
        Articulo reconstruido = ArticuloMapper.aDominio(ArticuloMapper.aEntidad(original));

        // Assert
        assertEquals(original.getId(), reconstruido.getId());
        assertEquals(original.getNombre(), reconstruido.getNombre());
        assertEquals(original.getCategoria(), reconstruido.getCategoria());
        assertEquals(original.getStock(), reconstruido.getStock());
        assertEquals(original.getMarca(), reconstruido.getMarca());
    }
}
