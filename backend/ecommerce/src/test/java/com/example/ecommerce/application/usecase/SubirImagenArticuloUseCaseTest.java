package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.AlmacenImagenes;
import com.example.ecommerce.application.AlmacenamientoImagenException;
import com.example.ecommerce.domain.valueobject.ArchivoImagen;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** RF-02.4: el caso de uso delega el guardado en el puerto AlmacenImagenes. */
class SubirImagenArticuloUseCaseTest {

    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 1};

    @Test
    void devuelveLaUrlQueEntregaElAlmacen() {
        // Arrange: un almacén falso, sin disco ni red (por eso existe el puerto).
        AlmacenImagenes almacenFalso = imagen -> "/imagenes/articulos/prueba." + imagen.getFormato().getExtension();
        SubirImagenArticuloUseCase useCase = new SubirImagenArticuloUseCase(almacenFalso);

        // Act
        String url = useCase.ejecutar(ArchivoImagen.desde(JPG));

        // Assert
        assertEquals("/imagenes/articulos/prueba.jpg", url);
    }

    @Test
    void siElAlmacenFallaSePropagaElErrorParaReintentarOContinuarSinImagen() {
        // Arrange (CU-01, curso alterno A4)
        AlmacenImagenes almacenCaido = imagen -> {
            throw new AlmacenamientoImagenException(new IOException("disco lleno"));
        };
        SubirImagenArticuloUseCase useCase = new SubirImagenArticuloUseCase(almacenCaido);

        // Act y Assert
        assertThrows(AlmacenamientoImagenException.class, () -> useCase.ejecutar(ArchivoImagen.desde(JPG)));
    }
}
