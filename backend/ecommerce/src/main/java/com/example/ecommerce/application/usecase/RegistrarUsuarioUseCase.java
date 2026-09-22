package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.ContrasenaRequeridaException;
import com.example.ecommerce.domain.exception.CorreoElectronicoDuplicadoException;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;

import java.util.UUID;

public class RegistrarUsuarioUseCase {

    private final UsuarioRepository repository;
    private final PasswordHasher passwordHasher;

    public RegistrarUsuarioUseCase(UsuarioRepository repository, PasswordHasher passwordHasher) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
    }

    public Usuario ejecutar(
            long id,
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

        Usuario usuario = new Usuario(id, nombre, correo, contrasenaHash, rolAsignado);

        repository.guardar(usuario);

        return usuario;
    }
}