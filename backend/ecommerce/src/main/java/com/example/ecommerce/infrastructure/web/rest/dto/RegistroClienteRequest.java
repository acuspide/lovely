package com.example.ecommerce.infrastructure.web.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Patrón: DTO — contrato de entrada del registro público. A propósito no
 * tiene campo "rol": el registro público siempre crea un CLIENTE (ver
 * AuthRestController); pedir un rol distinto requiere el endpoint protegido
 * de administración (CrearUsuarioInternoRequest).
 */
public record RegistroClienteRequest(
        @NotBlank(message = "El nombre es obligatorio.") String nombre,
        @NotBlank(message = "El correo es obligatorio.") @Email(message = "El correo no tiene un formato válido.") String email,
        @NotBlank(message = "La contraseña es obligatoria.") String contrasena,
        @NotBlank(message = "Debes confirmar la contraseña.") String confirmarContrasena) {
}
