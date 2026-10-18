package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.GestorTransacciones;
import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.CorreoElectronicoInvalidoException;
import com.example.ecommerce.domain.exception.CredencialesInvalidasException;
import com.example.ecommerce.domain.exception.CuentaBloqueadaException;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.exception.UsuarioInactivoException;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.Email;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Verifica correo y contraseña (RF-01.3) aplicando el bloqueo por intentos
 * fallidos (RF-01.4).
 *
 * El orden de las comprobaciones es deliberado:
 *  1. Correo desconocido y contraseña errada dan el mismo mensaje genérico, y
 *     en ambos casos se ejecuta bcrypt para que el tiempo de respuesta no
 *     delate si el correo existe.
 *  2. Una cuenta bloqueada se rechaza incluso con la contraseña correcta.
 *  3. Solo después de comprobar la contraseña se informa que la cuenta está
 *     desactivada, para no revelar ese dato a quien no la conoce.
 *
 * RF-01.4 exige que el contador no se pueda burlar con intentos simultáneos:
 * si diez peticiones leyeran a la vez "0 intentos", cada una guardaría "1" y
 * el bloqueo nunca llegaría. Por eso la cuenta se lee bloqueando su fila
 * dentro de una transacción, y los intentos sobre una misma cuenta se
 * atienden de uno en uno.
 */
public class AutenticarUsuarioUseCase {

    private static final String CONTRASENA_FICTICIA = "contrasena-ficticia-para-tiempo-constante-1";

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final GestorTransacciones transacciones;
    private final Clock reloj;
    private final String hashFicticio;

    public AutenticarUsuarioUseCase(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher,
                                    GestorTransacciones transacciones, Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.transacciones = transacciones;
        this.reloj = reloj;
        this.hashFicticio = passwordHasher.hash(CONTRASENA_FICTICIA);
    }

    public Usuario ejecutar(String email, String contrasenaPlano) {
        // El rechazo se lanza fuera de la transacción: si se lanzara dentro, se
        // revertiría también el intento fallido que se acaba de contar.
        Resultado resultado = transacciones.enTransaccion(() -> autenticar(email, contrasenaPlano));
        if (resultado.rechazo() != null) {
            throw resultado.rechazo();
        }
        return resultado.usuario();
    }

    private Resultado autenticar(String email, String contrasenaPlano) {
        Optional<Usuario> encontrada = buscarPorEmailParaActualizar(email);
        if (encontrada.isEmpty()) {
            passwordHasher.verificar(contrasenaPlano, hashFicticio);
            return Resultado.rechazo(new CredencialesInvalidasException());
        }

        Usuario usuario = encontrada.get();
        Instant ahora = reloj.instant();

        if (usuario.estaBloqueada(ahora)) {
            return Resultado.rechazo(new CuentaBloqueadaException(usuario.minutosRestantesDeBloqueo(ahora)));
        }

        if (!passwordHasher.verificar(contrasenaPlano, usuario.getContrasenaHash())) {
            return Resultado.rechazo(registrarIntentoFallido(usuario, ahora));
        }

        if (!usuario.isActivo()) {
            return Resultado.rechazo(new UsuarioInactivoException());
        }

        usuario.registrarIngresoExitoso();
        return Resultado.exito(usuarioRepository.guardar(usuario));
    }

    private Optional<Usuario> buscarPorEmailParaActualizar(String email) {
        try {
            return usuarioRepository.buscarPorEmailParaActualizar(new Email(email));
        } catch (CorreoElectronicoInvalidoException ex) {
            return Optional.empty();
        }
    }

    /**
     * Guarda el intento fallido y devuelve el error que corresponde: el quinto
     * intento ya informa el bloqueo; los anteriores, el mensaje genérico.
     */
    private ReglaDominioException registrarIntentoFallido(Usuario usuario, Instant ahora) {
        usuario.registrarIntentoFallido(ahora);
        usuarioRepository.guardar(usuario);

        if (usuario.estaBloqueada(ahora)) {
            return new CuentaBloqueadaException(usuario.minutosRestantesDeBloqueo(ahora));
        }
        return new CredencialesInvalidasException();
    }

    private record Resultado(Usuario usuario, ReglaDominioException rechazo) {

        static Resultado exito(Usuario usuario) {
            return new Resultado(usuario, null);
        }

        static Resultado rechazo(ReglaDominioException rechazo) {
            return new Resultado(null, rechazo);
        }
    }
}
