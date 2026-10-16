package com.example.ecommerce.support;

import com.example.ecommerce.application.GestorTransacciones;
import com.example.ecommerce.application.PasswordHasher;
import com.example.ecommerce.application.usecase.CrearCuentaPersonalUseCase;
import com.example.ecommerce.application.usecase.CrearCuentaPersonalUseCase.NuevaCuentaPersonal;
import com.example.ecommerce.application.usecase.RegistrarClienteUseCase;
import com.example.ecommerce.application.usecase.RegistrarClienteUseCase.SolicitudRegistro;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.service.ReglaAdministradoraActiva;
import com.example.ecommerce.domain.valueobject.RolUsuario;

import java.time.Duration;
import java.time.Instant;

/**
 * Reúne los dobles de prueba de F-01 y atajos para preparar cuentas, de modo
 * que cada prueba se concentre en el comportamiento que verifica.
 */
public class EntornoAutenticacion {

    public static final String CONTRASENA_VALIDA = "Lovely2026*";
    public static final Duration INACTIVIDAD_MAXIMA = Duration.ofMinutes(30);

    public final RelojControlable reloj = new RelojControlable(Instant.parse("2026-10-14T15:00:00Z"));
    public final UsuarioRepositoryEnMemoria usuarios = new UsuarioRepositoryEnMemoria();
    public final SesionRepositoryEnMemoria sesiones = new SesionRepositoryEnMemoria();
    public final TokenRecuperacionRepositoryEnMemoria tokensRecuperacion = new TokenRecuperacionRepositoryEnMemoria();
    public final PasswordHasher passwordHasher = new PasswordHasherFalso();
    public final TokenProviderFalso tokenProvider = new TokenProviderFalso();
    public final NotificadorCorreoFalso correo = new NotificadorCorreoFalso();
    public final GestorTransacciones transacciones = new GestorTransaccionesDirecto();
    public final ReglaAdministradoraActiva reglaAdministradoraActiva = new ReglaAdministradoraActiva(usuarios);

    public Usuario registrarCliente(String email) {
        return new RegistrarClienteUseCase(usuarios, passwordHasher, reloj).ejecutar(
                new SolicitudRegistro("Camila Ríos", email, "3001234567", CONTRASENA_VALIDA, true));
    }

    public Usuario crearPersonal(String email, RolUsuario rol) {
        return new CrearCuentaPersonalUseCase(usuarios, passwordHasher).ejecutar(
                new NuevaCuentaPersonal("Persona " + rol, email, null, CONTRASENA_VALIDA, rol));
    }

    public Usuario recargar(Usuario usuario) {
        return usuarios.obtenerPorId(usuario.getId()).orElseThrow();
    }
}
