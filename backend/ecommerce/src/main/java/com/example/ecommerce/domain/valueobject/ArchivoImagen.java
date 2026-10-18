package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.FormatoImagenNoPermitidoException;
import com.example.ecommerce.domain.exception.ImagenDemasiadoGrandeException;

/**
 * Patrón: Value Object — una imagen de producto que ya pasó las reglas de
 * RF-02.4. Si existe un {@code ArchivoImagen}, es seguro guardarlo.
 *
 * <ul>
 *   <li>Formato JPG, PNG o WEBP, reconocido por su contenido
 *       ({@link FormatoImagen#detectar}).</li>
 *   <li>Tamaño máximo de {@value #TAMANO_MAXIMO_MB} MB, para que el catálogo
 *       cargue rápido también en celulares.</li>
 * </ul>
 */
public final class ArchivoImagen {

    public static final int TAMANO_MAXIMO_MB = 2;
    public static final long TAMANO_MAXIMO_BYTES = TAMANO_MAXIMO_MB * 1024L * 1024L;

    private final byte[] contenido;
    private final FormatoImagen formato;

    private ArchivoImagen(byte[] contenido, FormatoImagen formato) {
        this.contenido = contenido;
        this.formato = formato;
    }

    /**
     * Valida el contenido recibido y crea el archivo de imagen.
     *
     * @throws ImagenDemasiadoGrandeException si supera {@value #TAMANO_MAXIMO_MB} MB
     * @throws FormatoImagenNoPermitidoException si está vacío o no es JPG, PNG ni WEBP
     */
    public static ArchivoImagen desde(byte[] contenido) {
        if (contenido == null || contenido.length == 0) {
            throw new FormatoImagenNoPermitidoException();
        }
        if (contenido.length > TAMANO_MAXIMO_BYTES) {
            throw new ImagenDemasiadoGrandeException(TAMANO_MAXIMO_MB);
        }
        FormatoImagen formato = FormatoImagen.detectar(contenido)
                .orElseThrow(FormatoImagenNoPermitidoException::new);
        return new ArchivoImagen(contenido.clone(), formato);
    }

    /** Copia defensiva: nadie de afuera puede modificar la imagen ya validada. */
    public byte[] getContenido() {
        return contenido.clone();
    }

    public FormatoImagen getFormato() {
        return formato;
    }

    public int getTamanoBytes() {
        return contenido.length;
    }
}
