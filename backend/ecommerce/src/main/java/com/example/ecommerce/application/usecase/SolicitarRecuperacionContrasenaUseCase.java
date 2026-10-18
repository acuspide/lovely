package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.GeneradorTokenSeguro;
import com.example.ecommerce.application.GestorTransacciones;
import com.example.ecommerce.application.NotificadorCorreo;
import com.example.ecommerce.domain.entity.TokenRecuperacion;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.repository.TokenRecuperacionRepository;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.Email;

import java.time.Clock;
import java.time.Instant;

/**
 * RF-01.8: envía por correo un enlace de un solo uso para restablecer la
 * contraseña.
 *
 * Responde igual exista o no la cuenta (el controlador siempre devuelve el
 * mismo mensaje), para que este formulario no sirva para averiguar qué correos
 * están registrados. Las cuentas desactivadas no reciben enlace.
 */
public class SolicitarRecuperacionContrasenaUseCase {

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionRepository tokenRepository;
    private final GeneradorTokenSeguro generadorToken;
    private final NotificadorCorreo notificadorCorreo;
    private final GestorTransacciones transacciones;
    private final Clock reloj;

    public SolicitarRecuperacionContrasenaUseCase(UsuarioRepository usuarioRepository,
                                                  TokenRecuperacionRepository tokenRepository,
                                                  GeneradorTokenSeguro generadorToken,
                                                  NotificadorCorreo notificadorCorreo,
                                                  GestorTransacciones transacciones,
                                                  Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.generadorToken = generadorToken;
        this.notificadorCorreo = notificadorCorreo;
        this.transacciones = transacciones;
        this.reloj = reloj;
    }

    public void ejecutar(String email) {
        usuarioRepository.buscarPorEmail(new Email(email))
                .filter(Usuario::isActivo)
                .ifPresent(this::enviarEnlace);
    }

    private void enviarEnlace(Usuario usuario) {
        Instant ahora = reloj.instant();
        String token = generadorToken.generar();

        transacciones.enTransaccion(() -> {
            tokenRepository.anularPendientesDe(usuario.getId(), ahora);
            tokenRepository.guardar(TokenRecuperacion.emitir(
                    usuario.getId(), generadorToken.calcularHuella(token), ahora));
        });

        notificadorCorreo.enviarEnlaceRecuperacion(usuario, token);
    }
}
