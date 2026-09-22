package com.example.ecommerce.infrastructure.web.rest;

import com.example.ecommerce.application.usecase.ActualizarArticuloUseCase;
import com.example.ecommerce.application.usecase.CrearArticuloUseCase;
import com.example.ecommerce.application.usecase.EliminarArticuloUseCase;
import com.example.ecommerce.application.usecase.ListarArticulosUseCase;
import com.example.ecommerce.application.usecase.ObtenerArticuloUseCase;
import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.infrastructure.web.rest.dto.ArticuloResponse;
import com.example.ecommerce.infrastructure.web.rest.dto.GuardarArticuloRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * F-02: Gestión del catálogo de productos (CRUD, RF-02). Las lecturas son
 * públicas (catálogo de clientas, RF-03); las escrituras son solo para
 * ADMINISTRADORA (CU-01), reforzado a nivel de método con @PreAuthorize y a
 * nivel de red en SecurityConfig (GET permitAll, el resto exige sesión).
 */
@RestController
@RequestMapping("/api/articulos")
public class ArticuloRestController {

    private final CrearArticuloUseCase crearArticuloUseCase;
    private final ActualizarArticuloUseCase actualizarArticuloUseCase;
    private final EliminarArticuloUseCase eliminarArticuloUseCase;
    private final ListarArticulosUseCase listarArticulosUseCase;
    private final ObtenerArticuloUseCase obtenerArticuloUseCase;

    public ArticuloRestController(CrearArticuloUseCase crearArticuloUseCase,
                                   ActualizarArticuloUseCase actualizarArticuloUseCase,
                                   EliminarArticuloUseCase eliminarArticuloUseCase,
                                   ListarArticulosUseCase listarArticulosUseCase,
                                   ObtenerArticuloUseCase obtenerArticuloUseCase) {
        this.crearArticuloUseCase = crearArticuloUseCase;
        this.actualizarArticuloUseCase = actualizarArticuloUseCase;
        this.eliminarArticuloUseCase = eliminarArticuloUseCase;
        this.listarArticulosUseCase = listarArticulosUseCase;
        this.obtenerArticuloUseCase = obtenerArticuloUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ArticuloResponse>> listar(
            @RequestParam(required = false) CategoriaArticulo categoria,
            @RequestParam(required = false) String buscar) {

        List<ArticuloResponse> articulos = listarArticulosUseCase
                .ejecutar(Optional.ofNullable(categoria), Optional.ofNullable(buscar))
                .stream()
                .map(ArticuloResponse::desde)
                .toList();

        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticuloResponse> obtener(@PathVariable long id) {
        Articulo articulo = obtenerArticuloUseCase.ejecutar(id);
        return ResponseEntity.ok(ArticuloResponse.desde(articulo));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADORA')")
    public ResponseEntity<ArticuloResponse> crear(@Valid @RequestBody GuardarArticuloRequest request) {
        Articulo articulo = crearArticuloUseCase.ejecutar(
                request.nombre(), request.descripcion(), new Precio(request.precio()),
                request.categoria(), request.marca(), request.imagenUrl(), request.stock());

        return ResponseEntity.status(HttpStatus.CREATED).body(ArticuloResponse.desde(articulo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADORA')")
    public ResponseEntity<ArticuloResponse> actualizar(@PathVariable long id,
                                                        @Valid @RequestBody GuardarArticuloRequest request) {
        Articulo articulo = actualizarArticuloUseCase.ejecutar(
                id, request.nombre(), request.descripcion(), new Precio(request.precio()),
                request.categoria(), request.marca(), request.imagenUrl(), request.stock());

        return ResponseEntity.ok(ArticuloResponse.desde(articulo));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADORA')")
    public ResponseEntity<Void> eliminar(@PathVariable long id) {
        eliminarArticuloUseCase.ejecutar(id);
        return ResponseEntity.noContent().build();
    }
}
