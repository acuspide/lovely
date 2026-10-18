package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.application.usecase.EditarCuentaPersonalUseCase.DatosCuentaPersonal;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Edición de una cuenta del personal (RF-01.2). No incluye la contraseña: la
 * persona la cambia desde su perfil o con "Olvidé mi contraseña" (RF-01.8).
 */
public record EditarCuentaPersonalRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres.") String nombre,
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 180, message = "El correo no puede superar 180 caracteres.") String email,
        String telefono,
        @NotNull(message = "El rol es obligatorio.") RolUsuario rol) {

    public DatosCuentaPersonal aDatos() {
        return new DatosCuentaPersonal(nombre, email, telefono, rol);
    }
}
