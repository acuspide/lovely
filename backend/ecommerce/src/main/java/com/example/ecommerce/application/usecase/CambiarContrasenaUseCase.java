package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.GestorTransacciones;
import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.ContrasenaActualIncorrectaException;
import com.example.ecommerce.domain.exception.ContrasenaRepetidaException;
import com.example.ecommerce.domain.exception.UsuarioNoEncontradoException;
import com.example.ecommerce.domain.repository.SesionRepository;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.service.PoliticaContrasena;

import java.time.Clock;

/**
 * RF-01.8: cambio de contraseña desde el perfil (IU-07). Exige la contraseña
 * actual y aplica las mismas reglas que el registro (RF-01.1).
 *
 * Conserva la sesión desde la que se hace el cambio y cierra las demás, por si
 * la contraseña anterior estaba comprometida.
 */
public class CambiarContrasenaUseCase {

    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;
    private final PasswordHasher passwordHasher;
    private final GestorTransacciones transacciones;
    private final Clock reloj;

    public CambiarContrasenaUseCase(UsuarioRepository usuarioRepository,
                                    SesionRepository sesionRepository,
                                    PasswordHasher passwordHasher,
                                    GestorTransacciones transacciones,
                                    Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.sesionRepository = sesionRepository;
        this.passwordHasher = passwordHasher;
        this.transacciones = transacciones;
        this.reloj = reloj;
    }

    public void ejecutar(long idUsuario, String idSesionActual, String contrasenaActual, String nuevaContrasena) {
        Usuario usuario = usuarioRepository.obtenerPorId(idUsuario)
                .orElseThrow(UsuarioNoEncontradoException::new);

        if (!passwordHasher.verificar(contrasenaActual, usuario.getContrasenaHash())) {
            throw new ContrasenaActualIncorrectaException();
        }
        PoliticaContrasena.validar(nuevaContrasena);
        if (passwordHasher.verificar(nuevaContrasena, usuario.getContrasenaHash())) {
            throw new ContrasenaRepetidaException();
        }

        usuario.cambiarContrasena(passwordHasher.hash(nuevaContrasena));

        transacciones.enTransaccion(() -> {
            usuarioRepository.guardar(usuario);
            sesionRepository.cerrarOtrasDeUsuario(idUsuario, idSesionActual, reloj.instant());
        });
    }
}
