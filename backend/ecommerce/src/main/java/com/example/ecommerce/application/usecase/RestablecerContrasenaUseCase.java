package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.GeneradorTokenSeguro;
import com.example.ecommerce.application.GestorTransacciones;
import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.domain.entity.TokenRecuperacion;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.EnlaceRecuperacionInvalidoException;
import com.example.ecommerce.domain.repository.SesionRepository;
import com.example.ecommerce.domain.repository.TokenRecuperacionRepository;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.service.PoliticaContrasena;

import java.time.Clock;
import java.time.Instant;

/**
 * RF-01.8: fija una contraseña nueva usando el enlace recibido por correo.
 *
 * La contraseña se valida antes de consumir el enlace, para que un error de
 * escritura no obligue a pedir otro. Al terminar se cierran todas las sesiones
 * abiertas: si alguien más conocía la contraseña anterior, pierde el acceso.
 */
public class RestablecerContrasenaUseCase {

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionRepository tokenRepository;
    private final SesionRepository sesionRepository;
    private final GeneradorTokenSeguro generadorToken;
    private final PasswordHasher passwordHasher;
    private final GestorTransacciones transacciones;
    private final Clock reloj;

    public RestablecerContrasenaUseCase(UsuarioRepository usuarioRepository,
                                        TokenRecuperacionRepository tokenRepository,
                                        SesionRepository sesionRepository,
                                        GeneradorTokenSeguro generadorToken,
                                        PasswordHasher passwordHasher,
                                        GestorTransacciones transacciones,
                                        Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.sesionRepository = sesionRepository;
        this.generadorToken = generadorToken;
        this.passwordHasher = passwordHasher;
        this.transacciones = transacciones;
        this.reloj = reloj;
    }

    public void ejecutar(String token, String nuevaContrasena) {
        PoliticaContrasena.validar(nuevaContrasena);

        transacciones.enTransaccion(() -> {
            Instant ahora = reloj.instant();

            TokenRecuperacion enlace = tokenRepository.buscarPorHuella(generadorToken.calcularHuella(token))
                    .orElseThrow(EnlaceRecuperacionInvalidoException::new);
            enlace.marcarUsado(ahora);

            Usuario usuario = usuarioRepository.obtenerPorId(enlace.getUsuarioId())
                    .filter(Usuario::isActivo)
                    .orElseThrow(EnlaceRecuperacionInvalidoException::new);
            usuario.cambiarContrasena(passwordHasher.hash(nuevaContrasena));

            tokenRepository.guardar(enlace);
            usuarioRepository.guardar(usuario);
            sesionRepository.cerrarTodasDeUsuario(usuario.getId(), ahora);
        });
    }
}
