package com.example.ecommerce.infrastructure.config;

import com.example.ecommerce.application.AlmacenImagenes;
import com.example.ecommerce.application.usecase.SubirImagenArticuloUseCase;
import com.example.ecommerce.infrastructure.almacenamiento.AlmacenImagenesDiscoLocal;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * RF-02.4: arma el almacenamiento de imágenes y publica la carpeta para que el
 * navegador pueda mostrarlas.
 *
 * <p>Aquí (y solo aquí) se decide qué adaptador implementa el puerto
 * {@link AlmacenImagenes}. Cambiar a un almacenamiento en la nube sería
 * cambiar este @Bean.</p>
 */
@Configuration
@EnableConfigurationProperties(PropiedadesImagenes.class)
public class ImagenesConfig implements WebMvcConfigurer {

    private final PropiedadesImagenes propiedades;

    public ImagenesConfig(PropiedadesImagenes propiedades) {
        this.propiedades = propiedades;
    }

    @Bean
    public AlmacenImagenesDiscoLocal almacenImagenes() {
        return new AlmacenImagenesDiscoLocal(Path.of(propiedades.directorio()), propiedades.rutaPublica());
    }

    @Bean
    public SubirImagenArticuloUseCase subirImagenArticuloUseCase(AlmacenImagenes almacenImagenes) {
        return new SubirImagenArticuloUseCase(almacenImagenes);
    }

    /** Sirve los archivos de la carpeta en {@code rutaPublica/**} (p. ej. /imagenes/articulos/abc.jpg). */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String carpeta = Path.of(propiedades.directorio()).toAbsolutePath().normalize().toUri().toString();
        String ruta = propiedades.rutaPublica().endsWith("/") ? propiedades.rutaPublica() : propiedades.rutaPublica() + "/";
        registry.addResourceHandler(ruta + "**")
                .addResourceLocations(carpeta.endsWith("/") ? carpeta : carpeta + "/");
    }
}
