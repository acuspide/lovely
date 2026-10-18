package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.domain.valueobject.Telefono;
import com.example.ecommerce.infrastructure.persistence.jpa.UsuarioJpaEntity;

/**
 * Patrón: Mapper — traduce entre el modelo de dominio (Usuario, ajeno a JPA)
 * y el modelo de persistencia (UsuarioJpaEntity, ajeno a las reglas de
 * negocio). Concentra en un solo lugar la conversión de enums, fechas y
 * objetos de valor, y evita que la persistencia se filtre hacia el dominio.
 */
public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static Usuario aDominio(UsuarioJpaEntity entidad) {
        return Usuario.reconstruir(
                entidad.getId(),
                entidad.getNombre(),
                new Email(entidad.getEmail()),
                entidad.getTelefono() == null ? null : new Telefono(entidad.getTelefono()),
                entidad.getContrasenaHash(),
                RolUsuario.valueOf(entidad.getRol().name()),
                entidad.isActivo(),
                entidad.getIntentosFallidos(),
                FechasUtc.aInstante(entidad.getBloqueadoHasta()),
                FechasUtc.aInstante(entidad.getHabeasDataAceptadoEn()));
    }

    public static UsuarioJpaEntity aEntidad(Usuario usuario) {
        // id = null cuando el usuario aún no existe en la base de datos:
        // Hibernate genera el id real con la secuencia.
        Long id = usuario.getId() == Usuario.SIN_PERSISTIR ? null : usuario.getId();

        return new UsuarioJpaEntity(
                id,
                usuario.getNombre(),
                usuario.getEmail().getValor(),
                usuario.getTelefono().map(Telefono::getValor).orElse(null),
                usuario.getContrasenaHash(),
                UsuarioJpaEntity.RolJpa.valueOf(usuario.getRol().name()),
                usuario.isActivo(),
                usuario.getIntentosFallidos(),
                FechasUtc.aColumna(usuario.getBloqueadoHasta().orElse(null)),
                FechasUtc.aColumna(usuario.getHabeasDataAceptadoEn().orElse(null)));
    }
}
