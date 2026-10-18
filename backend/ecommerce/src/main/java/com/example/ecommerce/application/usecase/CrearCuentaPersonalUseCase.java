package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.CorreoElectronicoDuplicadoException;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.service.PoliticaContrasena;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;

/**
 * RF-01.2: la Administradora crea la cuenta de una persona del equipo y le
 * asigna su rol. Que solo la Administradora pueda invocarlo se exige en la
 * capa web (@PreAuthorize); aquí se aplican las reglas de la cuenta.
 */
public class CrearCuentaPersonalUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;

    public CrearCuentaPersonalUseCase(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
    }

    public Usuario ejecutar(NuevaCuentaPersonal datos) {
        PoliticaContrasena.validar(datos.contrasena());

        Email correo = new Email(datos.email());
        if (usuarioRepository.existePorEmail(correo)) {
            throw new CorreoElectronicoDuplicadoException();
        }

        Usuario cuenta = Usuario.crearPersonal(
                datos.nombre(),
                correo,
                TelefonoOpcional.desde(datos.telefono()),
                passwordHasher.hash(datos.contrasena()),
                datos.rol());

        return usuarioRepository.guardar(cuenta);
    }

    public record NuevaCuentaPersonal(String nombre, String email, String telefono,
                                      String contrasena, RolUsuario rol) {

        /** RF-01.5: la contraseña nunca debe terminar en un log por accidente. */
        @Override
        public String toString() {
            return "NuevaCuentaPersonal{email=" + email + ", rol=" + rol + "}";
        }
    }
}
