package com.example.ecommerce.domain.valueobject;

import java.util.Arrays;
import java.util.Optional;

/**
 * RF-02.4: formatos de imagen permitidos para los productos (JPG, PNG y WEBP).
 *
 * <p>El formato se reconoce por la "firma" del archivo: los primeros bytes que
 * todo archivo de ese tipo tiene al inicio. No se confía en el nombre ni en la
 * extensión, porque cambiarle la extensión a un archivo (por ejemplo, un .exe
 * renombrado a .jpg) no lo convierte en una imagen.</p>
 */
public enum FormatoImagen {

    JPG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    WEBP("webp", "image/webp");

    private static final byte[] FIRMA_JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] FIRMA_PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final byte[] FIRMA_RIFF = {'R', 'I', 'F', 'F'};
    private static final byte[] FIRMA_WEBP = {'W', 'E', 'B', 'P'};
    private static final int POSICION_FIRMA_WEBP = 8;

    private final String extension;
    private final String tipoContenido;

    FormatoImagen(String extension, String tipoContenido) {
        this.extension = extension;
        this.tipoContenido = tipoContenido;
    }

    /** Detecta el formato a partir del contenido; vacío si no es JPG, PNG ni WEBP. */
    public static Optional<FormatoImagen> detectar(byte[] contenido) {
        if (empiezaCon(contenido, FIRMA_JPG, 0)) {
            return Optional.of(JPG);
        }
        if (empiezaCon(contenido, FIRMA_PNG, 0)) {
            return Optional.of(PNG);
        }
        if (empiezaCon(contenido, FIRMA_RIFF, 0) && empiezaCon(contenido, FIRMA_WEBP, POSICION_FIRMA_WEBP)) {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    private static boolean empiezaCon(byte[] contenido, byte[] firma, int desde) {
        if (contenido.length < desde + firma.length) {
            return false;
        }
        return Arrays.equals(contenido, desde, desde + firma.length, firma, 0, firma.length);
    }

    public String getExtension() {
        return extension;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }
}
