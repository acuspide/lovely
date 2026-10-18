package com.example.ecommerce.infrastructure.web.rest;

import com.example.ecommerce.application.usecase.CerrarSesionUseCase;
import com.example.ecommerce.application.usecase.IniciarSesionUseCase;
import com.example.ecommerce.application.usecase.RegistrarClienteUseCase;
import com.example.ecommerce.application.usecase.RestablecerContrasenaUseCase;
import com.example.ecommerce.application.usecase.SolicitarRecuperacionContrasenaUseCase;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.infrastructure.security.JwtAuthenticationFilter.UsuarioAutenticado;
import com.example.ecommerce.infrastructure.web.rest.dto.LoginRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.MensajeResponse;
import com.example.ecommerce.infrastructure.web.rest.dto.RecuperarContrasenaRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.RegistroClienteRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.RestablecerContrasenaRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.SesionRenovadaResponse;
import com.example.ecommerce.infrastructure.web.rest.dto.TokenResponse;
import com.example.ecommerce.infrastructure.web.rest.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada (REST) de la autenticación de F-01. Queda delgado a
 * propósito: solo traduce HTTP <-> casos de uso, sin reglas de negocio.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private static final String MENSAJE_RECUPERACION =
            "Si el correo corresponde a una cuenta activa, te enviamos un enlace para restablecer la contraseña. "
                    + "Revisa tu bandeja de entrada.";

    private final RegistrarClienteUseCase registrarClienteUseCase;
    private final IniciarSesionUseCase iniciarSesionUseCase;
    private final CerrarSesionUseCase cerrarSesionUseCase;
    private final SolicitarRecuperacionContrasenaUseCase solicitarRecuperacionUseCase;
    private final RestablecerContrasenaUseCase restablecerContrasenaUseCase;

    public AuthRestController(RegistrarClienteUseCase registrarClienteUseCase,
                              IniciarSesionUseCase iniciarSesionUseCase,
                              CerrarSesionUseCase cerrarSesionUseCase,
                              SolicitarRecuperacionContrasenaUseCase solicitarRecuperacionUseCase,
                              RestablecerContrasenaUseCase restablecerContrasenaUseCase) {
        this.registrarClienteUseCase = registrarClienteUseCase;
        this.iniciarSesionUseCase = iniciarSesionUseCase;
        this.cerrarSesionUseCase = cerrarSesionUseCase;
        this.solicitarRecuperacionUseCase = solicitarRecuperacionUseCase;
        this.restablecerContrasenaUseCase = restablecerContrasenaUseCase;
    }

    /** RF-01.1: registro público; siempre crea una cuenta con rol Cliente. */
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registro(@Valid @RequestBody RegistroClienteRequest request) {
        Usuario cliente = registrarClienteUseCase.ejecutar(request.aSolicitud());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(cliente));
    }

    /** RF-01.3 / RF-01.4: inicio de sesión con bloqueo por intentos fallidos. */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        IniciarSesionUseCase.SesionIniciada sesion =
                iniciarSesionUseCase.ejecutar(request.email(), request.contrasena());
        return ResponseEntity.ok(TokenResponse.desde(sesion));
    }

    /** RF-01.7: anula la sesión en el servidor; su token deja de servir. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UsuarioAutenticado principal) {
        cerrarSesionUseCase.ejecutar(principal.idSesion());
        return ResponseEntity.noContent().build();
    }

    /**
     * RF-01.6: "Seguir conectado". Toda petición autenticada ya renueva la
     * sesión (ver JwtAuthenticationFilter); este endpoint existe para que el
     * frontend pueda hacerlo sin pedir nada más.
     */
    @PostMapping("/sesion/renovar")
    public ResponseEntity<SesionRenovadaResponse> renovarSesion(@AuthenticationPrincipal UsuarioAutenticado principal) {
        return ResponseEntity.ok(new SesionRenovadaResponse(principal.vigenciaSesion().toSeconds()));
    }

    /** RF-01.8: siempre responde lo mismo, exista o no la cuenta. */
    @PostMapping("/recuperar-contrasena")
    public ResponseEntity<MensajeResponse> recuperarContrasena(@Valid @RequestBody RecuperarContrasenaRequest request) {
        solicitarRecuperacionUseCase.ejecutar(request.email());
        return ResponseEntity.accepted().body(new MensajeResponse(MENSAJE_RECUPERACION));
    }

    /** RF-01.8: fija la nueva contraseña con el enlace recibido por correo. */
    @PostMapping("/restablecer-contrasena")
    public ResponseEntity<MensajeResponse> restablecerContrasena(
            @Valid @RequestBody RestablecerContrasenaRequest request) {
        restablecerContrasenaUseCase.ejecutar(request.token(), request.nuevaContrasena());
        return ResponseEntity.ok(new MensajeResponse("Tu contraseña se actualizó. Ya puedes iniciar sesión."));
    }
}
