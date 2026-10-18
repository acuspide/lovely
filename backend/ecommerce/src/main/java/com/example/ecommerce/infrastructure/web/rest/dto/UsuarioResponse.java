package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.valueobject.Telefono;

/**
 * Patrón: DTO — nunca se expone Usuario (dominio) ni su hash de contraseña
 * por HTTP (RF-01.5); este record es la vista pública y segura de una cuenta.
 */
public record UsuarioResponse(long id, String nombre, String email, String telefono, String rol, boolean activo) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail().getValor(),
                usuario.getTelefono().map(Telefono::getValor).orElse(null),
                usuario.getRol().name(),
                usuario.isActivo());
    }
}
