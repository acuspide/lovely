package com.example.ecommerce.infrastructure.almacenamiento;

import com.example.ecommerce.domain.valueobject.ArchivoImagen;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF-02.4: el adaptador de disco guarda la imagen y devuelve su URL pública. */
class AlmacenImagenesDiscoLocalTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 1, 2, 3};

    @TempDir
    Path carpetaTemporal;

    @Test
    void guardaElArchivoConNombreAleatorioYExtensionDeSuFormatoReal() throws IOException {
        // Arrange
        AlmacenImagenesDiscoLocal almacen = new AlmacenImagenesDiscoLocal(carpetaTemporal, "/imagenes/articulos");

        // Act
        String url = almacen.guardar(ArchivoImagen.desde(PNG));

        // Assert
        assertTrue(url.startsWith("/imagenes/articulos/"), url);
        assertTrue(url.endsWith(".png"), url);
        String nombreArchivo = url.substring(url.lastIndexOf('/') + 1);
        assertArrayEquals(PNG, Files.readAllBytes(carpetaTemporal.resolve(nombreArchivo)));
    }

    @Test
    void dosSubidasDeLaMismaImagenNoSePisan() {
        // Arrange
        AlmacenImagenesDiscoLocal almacen = new AlmacenImagenesDiscoLocal(carpetaTemporal, "/imagenes/articulos/");

        // Act
        String primera = almacen.guardar(ArchivoImagen.desde(PNG));
        String segunda = almacen.guardar(ArchivoImagen.desde(PNG));

        // Assert
        assertNotEquals(primera, segunda);
    }

    @Test
    void creaLaCarpetaSiNoExiste() {
        // Arrange
        Path carpetaNueva = carpetaTemporal.resolve("uploads").resolve("articulos");

        // Act
        new AlmacenImagenesDiscoLocal(carpetaNueva, "/imagenes/articulos");

        // Assert
        assertTrue(Files.isDirectory(carpetaNueva));
    }
}
