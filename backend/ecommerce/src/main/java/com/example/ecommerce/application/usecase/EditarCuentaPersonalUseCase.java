package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.CorreoElectronicoDuplicadoException;
import com.example.ecommerce.domain.exception.UsuarioNoEncontradoException;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.service.ReglaAdministradoraActiva;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;

/**
 * RF-01.2: la Administradora edita los datos y el rol de una cuenta del
 * personal. Quitarle el rol a la única Administradora activa no está
 * permitido.
 */
public class EditarCuentaPersonalUseCase {

    private final UsuarioRepository usuarioRepository;
    private final ReglaAdministradoraActiva reglaAdministradoraActiva;

    public EditarCuentaPersonalUseCase(UsuarioRepository usuarioRepository,
                                       ReglaAdministradoraActiva reglaAdministradoraActiva) {
        this.usuarioRepository = usuarioRepository;
        this.reglaAdministradoraActiva = reglaAdministradoraActiva;
    }

    public Usuario ejecutar(long idUsuario, DatosCuentaPersonal datos) {
        Usuario cuenta = usuarioRepository.obtenerPorId(idUsuario)
                .orElseThrow(UsuarioNoEncontradoException::new);
        cuenta.verificarQueEsPersonal();

        Email nuevoCorreo = new Email(datos.email());
        if (!nuevoCorreo.equals(cuenta.getEmail()) && usuarioRepository.existePorEmail(nuevoCorreo)) {
            throw new CorreoElectronicoDuplicadoException();
        }

        if (datos.rol() != RolUsuario.ADMINISTRADORA) {
            reglaAdministradoraActiva.verificarQuePuedeDejarDeSerAdministradoraActiva(cuenta);
        }

        cuenta.actualizarDatosDePersonal(
                datos.nombre(), nuevoCorreo, TelefonoOpcional.desde(datos.telefono()), datos.rol());

        return usuarioRepository.guardar(cuenta);
    }

    public record DatosCuentaPersonal(String nombre, String email, String telefono, RolUsuario rol) {
    }
}
