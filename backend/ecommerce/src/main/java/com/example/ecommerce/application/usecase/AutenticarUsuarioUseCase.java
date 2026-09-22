package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.CredencialesInvalidasException;
import com.example.ecommerce.domain.exception.UsuarioInactivoException;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.Email;

public class AutenticarUsuarioUseCase {

    private final UsuarioRepository repository;
    private final PasswordHasher passwordHasher;

    public AutenticarUsuarioUseCase(UsuarioRepository repository, PasswordHasher passwordHasher) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
    }

    public Usuario ejecutar(String email, String contrasenaPlano) {
        Email correo = new Email(email);

        Usuario usuario = repository.buscarPorEmail(correo)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.isActivo()) {
            throw new UsuarioInactivoException();
        }

        if (!passwordHasher.verificar(contrasenaPlano, usuario.getContrasenaHash())) {
            throw new CredencialesInvalidasException();
        }

        return usuario;
    }
}
