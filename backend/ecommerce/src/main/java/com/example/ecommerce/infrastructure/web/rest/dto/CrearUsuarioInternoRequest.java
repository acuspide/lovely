package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.domain.valueobject.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * A diferencia de RegistroClienteRequest, este DTO sí trae "rol" y solo se
 * acepta en UsuarioAdminRestController, protegido con
 * @PreAuthorize("hasRole('ADMINISTRADORA')"). Así se evita duplicar la lógica
 * de registro solo para variar quién puede elegir el rol.
 */
public record CrearUsuarioInternoRequest(
        @NotBlank(message = "El nombre es obligatorio.") String nombre,
        @NotBlank(message = "El correo es obligatorio.") @Email(message = "El correo no tiene un formato válido.") String email,
        @NotBlank(message = "La contraseña es obligatoria.") String contrasena,
        @NotNull(message = "El rol es obligatorio.") RolUsuario rol) {
}
