package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.AlmacenImagenes;
import com.example.ecommerce.domain.valueobject.ArchivoImagen;

/**
 * RF-02.4: la Administradora sube la foto de un producto desde su equipo.
 *
 * <p>La subida es un paso separado de crear/editar el producto: primero se
 * sube la imagen y se obtiene su URL, y luego esa URL viaja con el resto del
 * formulario. Así, si la subida falla (CU-01, A4), la Administradora no
 * pierde lo que ya escribió: puede reintentar o guardar el producto sin
 * imagen.</p>
 *
 * <p>Las reglas de formato y tamaño ya las garantiza {@link ArchivoImagen};
 * este caso de uso solo coordina el guardado.</p>
 */
public class SubirImagenArticuloUseCase {

    private final AlmacenImagenes almacenImagenes;

    public SubirImagenArticuloUseCase(AlmacenImagenes almacenImagenes) {
        this.almacenImagenes = almacenImagenes;
    }

    /** @return URL pública de la imagen guardada */
    public String ejecutar(ArchivoImagen imagen) {
        return almacenImagenes.guardar(imagen);
    }
}
