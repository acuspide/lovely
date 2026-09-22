package com.example.ecommerce.infrastructure.web.rest;

import com.example.ecommerce.application.usecase.CambiarEstadoUsuarioUseCase;
import com.example.ecommerce.application.usecase.ListarUsuariosUseCase;
import com.example.ecommerce.application.usecase.RegistrarUsuarioUseCase;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.infrastructure.web.rest.dto.CambiarEstadoRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.CrearUsuarioInternoRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Panel de administración de usuarios (F-01: "gestión de roles"). Todo el
 * controlador exige el rol ADMINISTRADORA — es la única forma de crear
 * cuentas de personal interno (Asesora de Ventas, Encargada de Inventario,
 * u otra Administradora).
 */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMINISTRADORA')")
public class UsuarioAdminRestController {

    private final ListarUsuariosUseCase listarUsuariosUseCase;
    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase;

    public UsuarioAdminRestController(ListarUsuariosUseCase listarUsuariosUseCase,
                                       RegistrarUsuarioUseCase registrarUsuarioUseCase,
                                       CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase) {
        this.listarUsuariosUseCase = listarUsuariosUseCase;
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.cambiarEstadoUsuarioUseCase = cambiarEstadoUsuarioUseCase;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        List<UsuarioResponse> usuarios = listarUsuariosUseCase.ejecutar().stream()
                .map(UsuarioResponse::desde)
                .toList();

        return ResponseEntity.ok(usuarios);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioInternoRequest request) {
        Usuario usuario = registrarUsuarioUseCase.ejecutar(
                request.nombre(), request.email(), request.contrasena(), request.rol());

        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(usuario));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(@PathVariable long id,
                                                          @Valid @RequestBody CambiarEstadoRequest request) {
        Usuario usuario = cambiarEstadoUsuarioUseCase.ejecutar(id, request.activo());
        return ResponseEntity.ok(UsuarioResponse.desde(usuario));
    }
}
