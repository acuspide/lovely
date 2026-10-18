package com.example.ecommerce.infrastructure.web.rest.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

public record CambiarContrasenaRequest(
        @NotBlank(message = "La contraseña actual es obligatoria.") String contrasenaActual,
        @NotBlank(message = "La nueva contraseña es obligatoria.") String nuevaContrasena,
        @NotBlank(message = "Debes confirmar la nueva contraseña.") String confirmarContrasena) {

    @AssertTrue(message = "Las contraseñas no coinciden.")
    public boolean isConfirmacionCoincide() {
        return Objects.equals(nuevaContrasena, confirmarContrasena);
    }

    /** RF-01.5: las contraseñas nunca deben terminar en un log por accidente. */
    @Override
    public String toString() {
        return "CambiarContrasenaRequest{}";
    }
}
