package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.application.usecase.RegistrarClienteUseCase.SolicitudRegistro;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * Patrón: DTO — contrato de entrada del registro público (RF-01.1). A
 * propósito no tiene campo "rol": el registro público siempre crea una
 * Cliente. Las reglas de negocio (contraseña segura, correo único, Habeas
 * Data) las aplica el caso de uso; aquí solo se valida la forma.
 */
public record RegistroClienteRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres.") String nombre,
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 180, message = "El correo no puede superar 180 caracteres.") String email,
        @NotBlank(message = "El teléfono es obligatorio.") String telefono,
        @NotBlank(message = "La contraseña es obligatoria.") String contrasena,
        @NotBlank(message = "Debes confirmar la contraseña.") String confirmarContrasena,
        @NotNull(message = "Debes indicar si aceptas la política de tratamiento de datos.") Boolean aceptaHabeasData) {

    @AssertTrue(message = "Las contraseñas no coinciden.")
    public boolean isConfirmacionCoincide() {
        return Objects.equals(contrasena, confirmarContrasena);
    }

    public SolicitudRegistro aSolicitud() {
        return new SolicitudRegistro(nombre, email, telefono, contrasena, Boolean.TRUE.equals(aceptaHabeasData));
    }

    /** RF-01.5: las contraseñas nunca deben terminar en un log por accidente. */
    @Override
    public String toString() {
        return "RegistroClienteRequest{email=" + email + "}";
    }
}
