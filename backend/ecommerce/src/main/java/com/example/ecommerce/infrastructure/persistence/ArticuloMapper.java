package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.NombreArticulo;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.infrastructure.persistence.jpa.ArticuloJpaEntity;

/**
 * Patrón: Mapper — traduce entre Articulo (dominio) y ArticuloJpaEntity
 * (persistencia), igual que UsuarioMapper en F-01.
 */
public final class ArticuloMapper {

    private ArticuloMapper() {
    }

    public static Articulo aDominio(ArticuloJpaEntity entidad) {
        return new Articulo(
                entidad.getId(),
                new NombreArticulo(entidad.getNombre()),
                entidad.getDescripcion(),
                new Precio(entidad.getPrecio()),
                CategoriaArticulo.valueOf(entidad.getCategoria().name()),
                entidad.getMarca(),
                entidad.getImagenUrl(),
                entidad.getStock());
    }

    public static ArticuloJpaEntity aEntidad(Articulo articulo) {
        // id = null cuando el artículo aún no existe en la base de datos
        // (Articulo.getId() == 0): Hibernate genera el id real vía la secuencia.
        Long id = articulo.getId() == 0 ? null : articulo.getId();

        return new ArticuloJpaEntity(
                id,
                articulo.getNombre().getValor(),
                articulo.getDescripcion(),
                articulo.getPrecio().valor(),
                ArticuloJpaEntity.CategoriaJpa.valueOf(articulo.getCategoria().name()),
                articulo.getMarca(),
                articulo.getImagenUrl(),
                articulo.getStock());
    }
}
