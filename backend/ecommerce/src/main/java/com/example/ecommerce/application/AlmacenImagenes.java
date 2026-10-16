package com.example.ecommerce.application;

import com.example.ecommerce.domain.valueobject.ArchivoImagen;

/**
 * Puerto de salida (arquitectura hexagonal) para guardar las imágenes de los
 * productos (RF-02.4).
 *
 * <p>El caso de uso no sabe dónde terminan las imágenes. Hoy el adaptador las
 * guarda en una carpeta del servidor ({@code AlmacenImagenesDiscoLocal});
 * mañana podría ser un servicio en la nube (Cloudinary, S3…) escribiendo solo
 * un adaptador nuevo, sin tocar el dominio ni los casos de uso.</p>
 */
public interface AlmacenImagenes {

    /**
     * Guarda la imagen y devuelve la URL con la que se podrá mostrar.
     *
     * @throws AlmacenamientoImagenException si el almacén no pudo guardarla
     *         (disco lleno, servicio caído…). No es culpa de la usuaria: puede
     *         reintentar o continuar sin imagen (CU-01, curso alterno A4).
     */
    String guardar(ArchivoImagen imagen);
}
