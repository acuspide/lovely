package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.FormatoImagenNoPermitidoException;
import com.example.ecommerce.domain.exception.ImagenDemasiadoGrandeException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** RF-02.4: reglas de formato y tamaño de las imágenes de producto. */
class ArchivoImagenTest {

    /** Bytes mínimos que empiezan con la firma de cada formato. */
    static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10};
    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0};
    static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P'};

    @Test
    void reconoceJpgPngYWebpPorSuContenido() {
        assertEquals(FormatoImagen.JPG, ArchivoImagen.desde(JPG).getFormato());
        assertEquals(FormatoImagen.PNG, ArchivoImagen.desde(PNG).getFormato());
        assertEquals(FormatoImagen.WEBP, ArchivoImagen.desde(WEBP).getFormato());
    }

    @Test
    void rechazaArchivosQueNoSonImagenesAunqueSeLlamenJpg() {
        // Un PDF (o cualquier archivo renombrado a .jpg) no tiene la firma de una imagen.
        byte[] pdf = "%PDF-1.7 contenido".getBytes(StandardCharsets.US_ASCII);

        assertThrows(FormatoImagenNoPermitidoException.class, () -> ArchivoImagen.desde(pdf));
    }

    @Test
    void rechazaGifPorqueNoEstaEntreLosFormatosPermitidos() {
        byte[] gif = "GIF89a....".getBytes(StandardCharsets.US_ASCII);

        assertThrows(FormatoImagenNoPermitidoException.class, () -> ArchivoImagen.desde(gif));
    }

    @Test
    void rechazaUnArchivoVacio() {
        assertThrows(FormatoImagenNoPermitidoException.class, () -> ArchivoImagen.desde(new byte[0]));
        assertThrows(FormatoImagenNoPermitidoException.class, () -> ArchivoImagen.desde(null));
    }

    @Test
    void rechazaImagenesDeMasDeDosMegas() {
        byte[] grande = Arrays.copyOf(JPG, (int) ArchivoImagen.TAMANO_MAXIMO_BYTES + 1);

        assertThrows(ImagenDemasiadoGrandeException.class, () -> ArchivoImagen.desde(grande));
    }

    @Test
    void aceptaExactamenteDosMegas() {
        byte[] limite = Arrays.copyOf(JPG, (int) ArchivoImagen.TAMANO_MAXIMO_BYTES);

        assertEquals(ArchivoImagen.TAMANO_MAXIMO_BYTES, ArchivoImagen.desde(limite).getTamanoBytes());
    }

    @Test
    void elContenidoNoSePuedeModificarDesdeAfuera() {
        byte[] original = JPG.clone();
        ArchivoImagen imagen = ArchivoImagen.desde(original);

        original[0] = 0;
        imagen.getContenido()[1] = 0;

        assertArrayEquals(JPG, imagen.getContenido());
    }
}
