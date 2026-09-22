package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.domain.entity.Usuario;

/**
 * Patrón: DTO — nunca se expone Usuario (dominio) ni su hash de contraseña
 * directamente por HTTP; este record es la vista pública y segura de un
 * usuario.
 */
public record UsuarioResponse(long id, String nombre, String email, String rol, boolean activo) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail().getValor(),
                usuario.getRol().name(),
                usuario.isActivo());
    }
}
