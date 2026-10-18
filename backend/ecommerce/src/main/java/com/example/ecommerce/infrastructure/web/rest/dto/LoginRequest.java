package com.example.ecommerce.infrastructure.web.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El correo es obligatorio.") String email,
        @NotBlank(message = "La contraseña es obligatoria.") String contrasena) {

    /** RF-01.5: la contraseña nunca debe terminar en un log por accidente. */
    @Override
    public String toString() {
        return "LoginRequest{email=" + email + "}";
    }
}
