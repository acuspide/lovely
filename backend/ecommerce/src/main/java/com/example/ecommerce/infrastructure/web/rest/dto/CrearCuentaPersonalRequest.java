package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.application.usecase.CrearCuentaPersonalUseCase.NuevaCuentaPersonal;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Alta de una cuenta del personal (RF-01.2). Solo se acepta en
 * UsuarioAdminRestController, protegido para la Administradora. El teléfono
 * es opcional para el personal.
 */
public record CrearCuentaPersonalRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres.") String nombre,
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 180, message = "El correo no puede superar 180 caracteres.") String email,
        String telefono,
        @NotBlank(message = "La contraseña inicial es obligatoria.") String contrasena,
        @NotNull(message = "El rol es obligatorio.") RolUsuario rol) {

    public NuevaCuentaPersonal aNuevaCuenta() {
        return new NuevaCuentaPersonal(nombre, email, telefono, contrasena, rol);
    }

    /** RF-01.5: la contraseña nunca debe terminar en un log por accidente. */
    @Override
    public String toString() {
        return "CrearCuentaPersonalRequest{email=" + email + ", rol=" + rol + "}";
    }
}
