package com.example.ecommerce.infrastructure.web.rest;

import com.example.ecommerce.application.usecase.ActualizarArticuloUseCase;
import com.example.ecommerce.application.usecase.CambiarEstadoArticuloUseCase;
import com.example.ecommerce.application.usecase.CrearArticuloUseCase;
import com.example.ecommerce.application.usecase.EliminarArticuloUseCase;
import com.example.ecommerce.application.usecase.ListarArticulosUseCase;
import com.example.ecommerce.application.usecase.ObtenerArticuloUseCase;
import com.example.ecommerce.application.usecase.OrdenCatalogo;
import com.example.ecommerce.application.usecase.SubirImagenArticuloUseCase;
import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.valueobject.ArchivoImagen;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.infrastructure.web.rest.dto.ActualizarArticuloRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.ArticuloResponse;
import com.example.ecommerce.infrastructure.web.rest.dto.CambiarEstadoRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.CrearArticuloRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.ImagenSubidaResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * F-02: Gestión del catálogo de productos (CRUD, RF-02). Las lecturas son
 * públicas (catálogo de clientas, RF-03); las escrituras son solo para
 * ADMINISTRADORA (CU-01), reforzado a nivel de método con @PreAuthorize y a
 * nivel de red en SecurityConfig (GET permitAll, el resto exige sesión).
 *
 * <ul>
 *   <li>{@code GET /api/articulos} y {@code GET /api/articulos/{id}}: catálogo
 *       público, solo productos activos.</li>
 *   <li>{@code GET /api/articulos/gestion} y {@code GET /api/articulos/gestion/{id}}:
 *       panel de la Administradora (RF-02.5), incluye los productos
 *       desactivados (RF-02.7). Aunque sean GET, exigen el rol con @PreAuthorize.</li>
 *   <li>{@code PATCH /api/articulos/{id}/estado}: desactivar o reactivar (RF-02.7).</li>
 *   <li>{@code POST /api/articulos/imagenes}: subir la foto de un producto (RF-02.4).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/articulos")
public class ArticuloRestController {

    private final CrearArticuloUseCase crearArticuloUseCase;
    private final ActualizarArticuloUseCase actualizarArticuloUseCase;
    private final EliminarArticuloUseCase eliminarArticuloUseCase;
    private final ListarArticulosUseCase listarArticulosUseCase;
    private final ObtenerArticuloUseCase obtenerArticuloUseCase;
    private final CambiarEstadoArticuloUseCase cambiarEstadoArticuloUseCase;
    private final SubirImagenArticuloUseCase subirImagenArticuloUseCase;

    public ArticuloRestController(CrearArticuloUseCase crearArticuloUseCase,
                                   ActualizarArticuloUseCase actualizarArticuloUseCase,
                                   EliminarArticuloUseCase eliminarArticuloUseCase,
                                   ListarArticulosUseCase listarArticulosUseCase,
                                   ObtenerArticuloUseCase obtenerArticuloUseCase,
                                   CambiarEstadoArticuloUseCase cambiarEstadoArticuloUseCase,
                                   SubirImagenArticuloUseCase subirImagenArticuloUseCase) {
        this.crearArticuloUseCase = crearArticuloUseCase;
        this.actualizarArticuloUseCase = actualizarArticuloUseCase;
        this.eliminarArticuloUseCase = eliminarArticuloUseCase;
        this.listarArticulosUseCase = listarArticulosUseCase;
        this.obtenerArticuloUseCase = obtenerArticuloUseCase;
        this.cambiarEstadoArticuloUseCase = cambiarEstadoArticuloUseCase;
        this.subirImagenArticuloUseCase = subirImagenArticuloUseCase;
    }

    /**
     * Catálogo público (RF-03). {@code orden} acepta NOMBRE (por defecto),
     * PRECIO_MENOR o PRECIO_MAYOR (RF-03.6); los agotados siempre van al final.
     */
    @GetMapping
    public ResponseEntity<List<ArticuloResponse>> listar(
            @RequestParam(required = false) CategoriaArticulo categoria,
            @RequestParam(required = false) String buscar,
            @RequestParam(defaultValue = "NOMBRE") OrdenCatalogo orden) {

        List<ArticuloResponse> articulos = listarArticulosUseCase
                .ejecutar(Optional.ofNullable(categoria), Optional.ofNullable(buscar), orden)
                .stream()
                .map(ArticuloResponse::desde)
                .toList();

        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/gestion")
    @PreAuthorize("hasRole('ADMINISTRADORA')")
    public ResponseEntity<List<ArticuloResponse>> listarParaGestion(
            @RequestParam(required = false) CategoriaArticulo categoria,
            @RequestParam(required = false) String buscar) {

        List<ArticuloResponse> articulos = listarArticulosUseCase
                .ejecutarParaGestion(Optional.ofNullable(categoria), Optional.ofNullable(buscar))
                .stream()
                .map(ArticuloResponse::desde)
                .toList();

        return ResponseEntity.ok(articulos);
    }

    /** RF-02.5: detalle para el panel; a diferencia del público, incluye productos desactivados. */
    @GetMapping("/gestion/{id}")
    @PreAuthorize("hasRole('ADMINISTRADORA')")
    public ResponseEntity<ArticuloResponse> obtenerParaGestion(@PathVariable long id) {
        return ResponseEntity.ok(ArticuloResponse.desde(obtenerArticuloUseCase.ejecutarParaGestion(id)));
    }

    /**
     * RF-02.9: ¿se puede eliminar? La pantalla lo pregunta al presionar
     * "Eliminar" para avisar de inmediato si el producto tiene pedidos activos.
     */
    @GetMapping("/gestion/{id}/eliminacion")
    @PreAuthorize("hasRole('ADMINISTRADORA')")
    public ResponseEntity<Map<String, Boolean>> verificarEliminacion(@PathVariable long id) {
        return ResponseEntity.ok(Map.of("puedeEliminarse", eliminarArticuloUseCase.puedeEliminarse(id)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticuloResponse> obtener(@PathVariable long id) {
        Articulo articulo = obtenerArticuloUseCase.ejecutar(id);
        return ResponseEntity.ok(ArticuloResponse.desde(articulo));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADORA')")
    public ResponseEntity<ArticuloResponse> crear(@Valid @RequestBody CrearArticuloRequest request) {
        Articulo articulo = crearArticuloUseCase.ejecutar(
                request.nombre(), request.descripcion(), new Precio(request.precio()),
                request.categoria(), request.marca(), request.imagenUrl(), request.stockInicial());

        return ResponseEntity.status(HttpStatus.CREATED).body(ArticuloResponse.desde(articulo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADORA')")
    public ResponseEntity<ArticuloResponse> actualizar(@PathVariable long id,
                                                        @Valid @RequestBody ActualizarArticuloRequest request) {
        // RF-02.6: la edición no toca el stock (lo gestiona el módulo de inventario).
        Articulo articulo = actualizarArticuloUseCase.ejecutar(
                id, request.nombre(), request.descripcion(), new Precio(request.precio()),
                request.categoria(), request.marca(), request.imagenUrl());

        return ResponseEntity.ok(ArticuloResponse.desde(articulo));
    }

    /**
     * RF-02.4: recibe la imagen como formulario multipart (campo "imagen") y
     * devuelve su URL. El tamaño máximo lo frena primero Spring
     * (spring.servlet.multipart.max-file-size) y el formato y tamaño los
     * vuelve a validar el dominio en {@link ArchivoImagen}.
     */
    @PostMapping(path = "/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADORA')")
    public ResponseEntity<ImagenSubidaResponse> subirImagen(@RequestParam("imagen") MultipartFile imagen)
            throws IOException {
        String url = subirImagenArticuloUseCase.ejecutar(ArchivoImagen.desde(imagen.getBytes()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new ImagenSubidaResponse(url));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADORA')")
    public ResponseEntity<ArticuloResponse> cambiarEstado(@PathVariable long id,
                                                          @Valid @RequestBody CambiarEstadoRequest request) {
        Articulo articulo = cambiarEstadoArticuloUseCase.ejecutar(id, request.activo());
        return ResponseEntity.ok(ArticuloResponse.desde(articulo));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADORA')")
    public ResponseEntity<Void> eliminar(@PathVariable long id) {
        eliminarArticuloUseCase.ejecutar(id);
        return ResponseEntity.noContent().build();
    }
}
