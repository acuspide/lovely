package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.ContrasenaRequeridaException;
import com.example.ecommerce.domain.exception.CorreoElectronicoDuplicadoException;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;

/**
 * Caso de uso único para registrar cualquier usuario del sistema, sin
 * importar el rol. Lo reutilizan dos adaptadores de entrada con reglas de
 * autorización distintas (SRP: la política de "quién puede pedir qué rol" no
 * es responsabilidad de este caso de uso, sino de quien lo invoca):
 *  - AuthRestController (público): siempre pasa RolUsuario.CLIENTE.
 *  - UsuarioAdminRestController (protegido, solo ADMINISTRADORA): puede pasar
 *    cualquier rol, protegido con @PreAuthorize en la capa web.
 */
public class RegistrarUsuarioUseCase {

    private final UsuarioRepository repository;
    private final PasswordHasher passwordHasher;

    public RegistrarUsuarioUseCase(UsuarioRepository repository, PasswordHasher passwordHasher) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
    }

    public Usuario ejecutar(
            String nombre,
            String email,
            String contrasenaPlano,
            RolUsuario rol) {

        if (contrasenaPlano == null || contrasenaPlano.trim().isEmpty()) {
            throw new ContrasenaRequeridaException();
        }

        Email correo = new Email(email);

        // RN01: el correo electrónico debe ser único.
        if (repository.existePorEmail(correo)) {
            throw new CorreoElectronicoDuplicadoException();
        }

        // RN03: si no se indica rol, se asume Cliente por defecto.
        RolUsuario rolAsignado = rol != null ? rol : RolUsuario.CLIENTE;

        String contrasenaHash = passwordHasher.hash(contrasenaPlano);

        // id = 0: "sin persistir". El repositorio asigna el id real (secuencia
        // de Oracle en producción, contador en memoria en los tests) y
        // devuelve la entidad ya con su identidad definitiva.
        Usuario usuarioSinPersistir = new Usuario(0L, nombre, correo, contrasenaHash, rolAsignado);

        return repository.guardar(usuarioSinPersistir);
    }
}