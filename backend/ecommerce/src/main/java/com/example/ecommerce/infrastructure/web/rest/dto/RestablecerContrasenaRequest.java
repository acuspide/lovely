package com.example.ecommerce.infrastructure.web.rest.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

public record RestablecerContrasenaRequest(
        @NotBlank(message = "El enlace de recuperación no es válido.") String token,
        @NotBlank(message = "La nueva contraseña es obligatoria.") String nuevaContrasena,
        @NotBlank(message = "Debes confirmar la nueva contraseña.") String confirmarContrasena) {

    @AssertTrue(message = "Las contraseñas no coinciden.")
    public boolean isConfirmacionCoincide() {
        return Objects.equals(nuevaContrasena, confirmarContrasena);
    }

    /** El token y la contraseña dan acceso a la cuenta: nunca en los logs. */
    @Override
    public String toString() {
        return "RestablecerContrasenaRequest{}";
    }
}
