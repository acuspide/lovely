package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.infrastructure.persistence.jpa.UsuarioJpaEntity;

/**
 * Patrón: Mapper — traduce entre el modelo de dominio (Usuario, ajeno a JPA)
 * y el modelo de persistencia (UsuarioJpaEntity, ajeno a las reglas de
 * negocio). Concentra en un solo lugar la conversión de enums y evita que la
 * capa de persistencia se filtre hacia el dominio o viceversa.
 */
public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static Usuario aDominio(UsuarioJpaEntity entidad) {
        Usuario usuario = new Usuario(
                entidad.getId(),
                entidad.getNombre(),
                new Email(entidad.getEmail()),
                entidad.getContrasenaHash(),
                RolUsuario.valueOf(entidad.getRol().name()));

        if (!entidad.isActivo()) {
            usuario.desactivar();
        }
        return usuario;
    }

    public static UsuarioJpaEntity aEntidad(Usuario usuario) {
        // id = null cuando el usuario aún no existe en la base de datos
        // (Usuario.getId() == 0): Hibernate genera el id real vía la secuencia.
        Long id = usuario.getId() == 0 ? null : usuario.getId();

        return new UsuarioJpaEntity(
                id,
                usuario.getNombre(),
                usuario.getEmail().getValor(),
                usuario.getContrasenaHash(),
                UsuarioJpaEntity.RolJpa.valueOf(usuario.getRol().name()),
                usuario.isActivo());
    }
}
