package com.example.ecommerce.infrastructure.web.rest;

import com.example.ecommerce.application.usecase.ObtenerPedidoDeClienteUseCase;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase.Linea;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase.Resultado;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase.Solicitud;
import com.example.ecommerce.domain.valueobject.DatosEntrega;
import com.example.ecommerce.infrastructure.security.JwtAuthenticationFilter.UsuarioAutenticado;
import com.example.ecommerce.infrastructure.web.rest.dto.PedidoResponse;
import com.example.ecommerce.infrastructure.web.rest.dto.RegistrarPedidoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * F-05: pedidos de la clienta. Ambos endpoints exigen rol CLIENTE (RN21) y
 * trabajan sobre la clienta de la sesión: el id nunca viene de la petición.
 *
 * <ul>
 *   <li>{@code POST /api/pedidos}: registra el pedido. Responde 201 si es
 *       nuevo y 200 si era un reintento del mismo intento de compra (RF-05.7).</li>
 *   <li>{@code GET /api/pedidos/{id}}: consulta un pedido propio (RF-05.5).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/pedidos")
@PreAuthorize("hasRole('CLIENTE')")
public class PedidoRestController {

    private final RegistrarPedidoUseCase registrarPedidoUseCase;
    private final ObtenerPedidoDeClienteUseCase obtenerPedidoDeClienteUseCase;

    public PedidoRestController(RegistrarPedidoUseCase registrarPedidoUseCase,
                                ObtenerPedidoDeClienteUseCase obtenerPedidoDeClienteUseCase) {
        this.registrarPedidoUseCase = registrarPedidoUseCase;
        this.obtenerPedidoDeClienteUseCase = obtenerPedidoDeClienteUseCase;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> registrar(@AuthenticationPrincipal UsuarioAutenticado clienta,
                                                    @Valid @RequestBody RegistrarPedidoRequest request) {
        var entrega = request.entrega();
        Solicitud solicitud = new Solicitud(
                clienta.id(),
                request.lineas().stream().map(l -> new Linea(l.articuloId(), l.cantidad())).toList(),
                new DatosEntrega(entrega.tipo(), entrega.nombreContacto(), entrega.telefono(),
                        entrega.direccion(), entrega.ciudad(), entrega.indicaciones()),
                request.claveIdempotencia());

        Resultado resultado = registrarPedidoUseCase.ejecutar(solicitud);
        return ResponseEntity.status(resultado.nuevo() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(PedidoResponse.desde(resultado.pedido()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtener(@AuthenticationPrincipal UsuarioAutenticado clienta,
                                                  @PathVariable long id) {
        return ResponseEntity.ok(PedidoResponse.desde(obtenerPedidoDeClienteUseCase.ejecutar(id, clienta.id())));
    }
}
