package com.example.ecommerce.infrastructure.web.rest;

import com.example.ecommerce.application.usecase.CambiarEstadoUsuarioUseCase;
import com.example.ecommerce.application.usecase.CrearCuentaPersonalUseCase;
import com.example.ecommerce.application.usecase.EditarCuentaPersonalUseCase;
import com.example.ecommerce.application.usecase.ListarPersonalUseCase;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.infrastructure.web.rest.dto.CambiarEstadoRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.CrearCuentaPersonalRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.EditarCuentaPersonalRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Gestión de las cuentas del personal (RF-01.2). Todo el controlador exige el
 * rol Administradora (RNF-06). Las cuentas no se eliminan: no hay DELETE, solo
 * desactivación, para conservar su historial.
 */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMINISTRADORA')")
public class UsuarioAdminRestController {

    private final ListarPersonalUseCase listarPersonalUseCase;
    private final CrearCuentaPersonalUseCase crearCuentaPersonalUseCase;
    private final EditarCuentaPersonalUseCase editarCuentaPersonalUseCase;
    private final CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase;

    public UsuarioAdminRestController(ListarPersonalUseCase listarPersonalUseCase,
                                      CrearCuentaPersonalUseCase crearCuentaPersonalUseCase,
                                      EditarCuentaPersonalUseCase editarCuentaPersonalUseCase,
                                      CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase) {
        this.listarPersonalUseCase = listarPersonalUseCase;
        this.crearCuentaPersonalUseCase = crearCuentaPersonalUseCase;
        this.editarCuentaPersonalUseCase = editarCuentaPersonalUseCase;
        this.cambiarEstadoUsuarioUseCase = cambiarEstadoUsuarioUseCase;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        List<UsuarioResponse> personal = listarPersonalUseCase.ejecutar().stream()
                .map(UsuarioResponse::desde)
                .toList();
        return ResponseEntity.ok(personal);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearCuentaPersonalRequest request) {
        Usuario cuenta = crearCuentaPersonalUseCase.ejecutar(request.aNuevaCuenta());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(cuenta));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> editar(@PathVariable long id,
                                                  @Valid @RequestBody EditarCuentaPersonalRequest request) {
        Usuario cuenta = editarCuentaPersonalUseCase.ejecutar(id, request.aDatos());
        return ResponseEntity.ok(UsuarioResponse.desde(cuenta));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(@PathVariable long id,
                                                         @Valid @RequestBody CambiarEstadoRequest request) {
        Usuario cuenta = cambiarEstadoUsuarioUseCase.ejecutar(id, request.activo());
        return ResponseEntity.ok(UsuarioResponse.desde(cuenta));
    }
}
