package com.example.ecommerce.infrastructure.almacenamiento;

import com.example.ecommerce.application.AlmacenImagenes;
import com.example.ecommerce.application.AlmacenamientoImagenException;
import com.example.ecommerce.domain.valueobject.ArchivoImagen;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/**
 * Patrón: Adapter — implementa el puerto {@link AlmacenImagenes} guardando
 * las imágenes en una carpeta del servidor (RF-02.4).
 *
 * <ul>
 *   <li>Cada archivo recibe un nombre aleatorio (UUID) + la extensión de su
 *       formato real. Nunca se usa el nombre que trae el archivo subido: así
 *       no hay choques entre imágenes y nadie puede "escaparse" de la carpeta
 *       con nombres como {@code ../../config}.</li>
 *   <li>La carpeta se publica en {@code rutaPublica} (ver
 *       {@code ImagenesWebConfig}), y esa es la URL que se guarda en el producto.</li>
 * </ul>
 */
public class AlmacenImagenesDiscoLocal implements AlmacenImagenes {

    private final Path directorio;
    private final String rutaPublica;

    /**
     * @param directorio  carpeta donde se escriben los archivos (se crea si no existe)
     * @param rutaPublica prefijo de URL con el que se sirven, p. ej. {@code /imagenes/articulos}
     */
    public AlmacenImagenesDiscoLocal(Path directorio, String rutaPublica) {
        this.directorio = directorio.toAbsolutePath().normalize();
        this.rutaPublica = rutaPublica.endsWith("/") ? rutaPublica : rutaPublica + "/";
        crearDirectorio();
    }

    @Override
    public String guardar(ArchivoImagen imagen) {
        String nombreArchivo = UUID.randomUUID() + "." + imagen.getFormato().getExtension();
        try {
            // CREATE_NEW: falla en lugar de sobrescribir si (muy improbable) el nombre ya existe.
            Files.write(directorio.resolve(nombreArchivo), imagen.getContenido(), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new AlmacenamientoImagenException(e);
        }
        return rutaPublica + nombreArchivo;
    }

    public Path getDirectorio() {
        return directorio;
    }

    private void crearDirectorio() {
        try {
            Files.createDirectories(directorio);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear la carpeta de imágenes: " + directorio, e);
        }
    }
}
