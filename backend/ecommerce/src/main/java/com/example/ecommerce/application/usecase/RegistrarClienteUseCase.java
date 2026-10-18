package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.CorreoElectronicoDuplicadoException;
import com.example.ecommerce.domain.exception.HabeasDataNoAceptadoException;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.service.PoliticaContrasena;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.Telefono;

import java.time.Clock;

/**
 * RF-01.1: una clienta crea su propia cuenta. El rol Cliente se asigna
 * siempre; el personal no puede registrarse por esta vía (RF-01.2).
 */
public class RegistrarClienteUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final Clock reloj;

    public RegistrarClienteUseCase(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher, Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.reloj = reloj;
    }

    public Usuario ejecutar(SolicitudRegistro solicitud) {
        if (!solicitud.aceptaHabeasData()) {
            throw new HabeasDataNoAceptadoException();
        }
        PoliticaContrasena.validar(solicitud.contrasena());

        Email correo = new Email(solicitud.email());
        Telefono telefono = new Telefono(solicitud.telefono());

        if (usuarioRepository.existePorEmail(correo)) {
            throw new CorreoElectronicoDuplicadoException();
        }

        Usuario cliente = Usuario.registrarCliente(
                solicitud.nombre(),
                correo,
                telefono,
                passwordHasher.hash(solicitud.contrasena()),
                reloj.instant());

        return usuarioRepository.guardar(cliente);
    }

    public record SolicitudRegistro(String nombre, String email, String telefono,
                                    String contrasena, boolean aceptaHabeasData) {

        /** RF-01.5: la contraseña nunca debe terminar en un log por accidente. */
        @Override
        public String toString() {
            return "SolicitudRegistro{email=" + email + ", aceptaHabeasData=" + aceptaHabeasData + "}";
        }
    }
}
