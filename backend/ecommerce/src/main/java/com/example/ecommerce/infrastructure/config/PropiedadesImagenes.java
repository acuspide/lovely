package com.example.ecommerce.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Parámetros de RF-02.4 leídos de application.properties (prefijo
 * {@code app.imagenes}).
 *
 * @param directorio  carpeta del servidor donde se guardan las imágenes de los productos
 * @param rutaPublica URL relativa con la que el navegador las pide (debe empezar por "/")
 */
@Validated
@ConfigurationProperties(prefix = "app.imagenes")
public record PropiedadesImagenes(
        @NotBlank String directorio,
        @NotBlank @Pattern(regexp = "^/.*", message = "debe empezar por /") String rutaPublica) {
}
