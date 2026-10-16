package com.example.ecommerce.infrastructure.web.rest;

import com.example.ecommerce.application.usecase.CambiarContrasenaUseCase;
import com.example.ecommerce.application.usecase.ObtenerPerfilUseCase;
import com.example.ecommerce.infrastructure.security.JwtAuthenticationFilter.UsuarioAutenticado;
import com.example.ecommerce.infrastructure.web.rest.dto.CambiarContrasenaRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.MensajeResponse;
import com.example.ecommerce.infrastructure.web.rest.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Perfil de la cuenta con sesión iniciada (IU-07), disponible para todos los
 * roles. Solo opera sobre la propia cuenta: el id sale de la sesión, nunca de
 * la URL, así que nadie puede consultar ni cambiar la cuenta de otra persona.
 */
@RestController
@RequestMapping("/api/perfil")
public class PerfilRestController {

    private final ObtenerPerfilUseCase obtenerPerfilUseCase;
    private final CambiarContrasenaUseCase cambiarContrasenaUseCase;

    public PerfilRestController(ObtenerPerfilUseCase obtenerPerfilUseCase,
                                CambiarContrasenaUseCase cambiarContrasenaUseCase) {
        this.obtenerPerfilUseCase = obtenerPerfilUseCase;
        this.cambiarContrasenaUseCase = cambiarContrasenaUseCase;
    }

    @GetMapping
    public ResponseEntity<UsuarioResponse> obtener(@AuthenticationPrincipal UsuarioAutenticado principal) {
        return ResponseEntity.ok(UsuarioResponse.desde(obtenerPerfilUseCase.ejecutar(principal.id())));
    }

    /** RF-01.8: cambio de contraseña pidiendo la actual. */
    @PutMapping("/contrasena")
    public ResponseEntity<MensajeResponse> cambiarContrasena(@AuthenticationPrincipal UsuarioAutenticado principal,
                                                             @Valid @RequestBody CambiarContrasenaRequest request) {
        cambiarContrasenaUseCase.ejecutar(
                principal.id(), principal.idSesion(), request.contrasenaActual(), request.nuevaContrasena());
        return ResponseEntity.ok(new MensajeResponse("Tu contraseña se actualizó correctamente."));
    }
}
