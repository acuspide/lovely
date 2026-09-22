package com.example.ecommerce.infrastructure.web.rest;

import com.example.ecommerce.application.usecase.IniciarSesionUseCase;
import com.example.ecommerce.application.usecase.RegistrarUsuarioUseCase;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.ContrasenasNoCoincidenException;
import com.example.ecommerce.domain.exception.UsuarioNoEncontradoException;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.infrastructure.security.JwtAuthenticationFilter.UsuarioAutenticado;
import com.example.ecommerce.infrastructure.web.rest.dto.LoginRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.RegistroClienteRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.TokenResponse;
import com.example.ecommerce.infrastructure.web.rest.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada (REST) de F-01. Queda deliberadamente delgado: solo
 * traduce HTTP <-> casos de uso, sin reglas de negocio propias.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final IniciarSesionUseCase iniciarSesionUseCase;
    private final UsuarioRepository usuarioRepository;

    public AuthRestController(RegistrarUsuarioUseCase registrarUsuarioUseCase,
                               IniciarSesionUseCase iniciarSesionUseCase,
                               UsuarioRepository usuarioRepository) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.iniciarSesionUseCase = iniciarSesionUseCase;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registro(@Valid @RequestBody RegistroClienteRequest request) {
        if (!request.contrasena().equals(request.confirmarContrasena())) {
            throw new ContrasenasNoCoincidenException();
        }

        // Registro público: siempre CLIENTE, sin importar lo que pida el
        // cliente HTTP (la única forma de crear otros roles es el endpoint
        // protegido de administración).
        Usuario usuario = registrarUsuarioUseCase.ejecutar(
                request.nombre(), request.email(), request.contrasena(), RolUsuario.CLIENTE);

        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        IniciarSesionUseCase.SesionIniciada sesion =
                iniciarSesionUseCase.ejecutar(request.email(), request.contrasena());

        return ResponseEntity.ok(TokenResponse.desde(sesion));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> perfilActual(@AuthenticationPrincipal UsuarioAutenticado principal) {
        Usuario usuario = usuarioRepository.obtenerPorId(principal.id())
                .orElseThrow(UsuarioNoEncontradoException::new);

        return ResponseEntity.ok(UsuarioResponse.desde(usuario));
    }
}
