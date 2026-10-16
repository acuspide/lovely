package com.example.ecommerce.infrastructure.web.rest;

import com.example.ecommerce.application.usecase.CancelarPedidoUseCase;
import com.example.ecommerce.application.usecase.ListarPedidosDeClienteUseCase;
import com.example.ecommerce.application.usecase.ObtenerPedidoDeClienteUseCase;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase.Linea;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase.Resultado;
import com.example.ecommerce.application.usecase.RegistrarPedidoUseCase.Solicitud;
import com.example.ecommerce.domain.valueobject.DatosEntrega;
import com.example.ecommerce.domain.valueobject.QuienCancela;
import com.example.ecommerce.infrastructure.security.JwtAuthenticationFilter.UsuarioAutenticado;
import com.example.ecommerce.infrastructure.web.rest.dto.CancelarPedidoRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.PedidoResponse;
import com.example.ecommerce.infrastructure.web.rest.dto.RegistrarPedidoRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.ResumenPedidoClienteResponse;
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

import java.util.List;

/**
 * F-05 y F-07: pedidos de la clienta. Todos los endpoints exigen rol CLIENTE
 * (RN21) y trabajan sobre la clienta de la sesión: su id nunca viene de la
 * petición.
 *
 * <ul>
 *   <li>{@code POST /api/pedidos}: registra el pedido. Responde 201 si es
 *       nuevo y 200 si era un reintento del mismo intento de compra (RF-05.7).</li>
 *   <li>{@code GET /api/pedidos/{id}}: consulta un pedido propio (RF-05.5).</li>
 *   <li>{@code GET /api/pedidos}: "Mis pedidos" con el estado de cada uno (RF-07.8).</li>
 *   <li>{@code POST /api/pedidos/{id}/cancelacion}: cancela un pedido propio
 *       Pendiente, con motivo (RF-07.5), y devuelve el stock (RF-07.6).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/pedidos")
@PreAuthorize("hasRole('CLIENTE')")
public class PedidoRestController {

    private final RegistrarPedidoUseCase registrarPedidoUseCase;
    private final ObtenerPedidoDeClienteUseCase obtenerPedidoDeClienteUseCase;

    private final ListarPedidosDeClienteUseCase listarPedidosDeClienteUseCase;
    private final CancelarPedidoUseCase cancelarPedidoUseCase;

    public PedidoRestController(RegistrarPedidoUseCase registrarPedidoUseCase,
                                ObtenerPedidoDeClienteUseCase obtenerPedidoDeClienteUseCase,
                                ListarPedidosDeClienteUseCase listarPedidosDeClienteUseCase,
                                CancelarPedidoUseCase cancelarPedidoUseCase) {
        this.registrarPedidoUseCase = registrarPedidoUseCase;
        this.obtenerPedidoDeClienteUseCase = obtenerPedidoDeClienteUseCase;
        this.listarPedidosDeClienteUseCase = listarPedidosDeClienteUseCase;
        this.cancelarPedidoUseCase = cancelarPedidoUseCase;
    }

    @GetMapping
    public List<ResumenPedidoClienteResponse> listarMisPedidos(@AuthenticationPrincipal UsuarioAutenticado clienta) {
        return listarPedidosDeClienteUseCase.ejecutar(clienta.id()).stream()
                .map(ResumenPedidoClienteResponse::desde)
                .toList();
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

    @PostMapping("/{id}/cancelacion")
    public PedidoResponse cancelar(@AuthenticationPrincipal UsuarioAutenticado clienta, @PathVariable long id,
                                   @Valid @RequestBody CancelarPedidoRequest request) {
        var solicitud = new CancelarPedidoUseCase.Solicitud(id, QuienCancela.CLIENTA, clienta.id(), request.motivo());
        return PedidoResponse.desde(cancelarPedidoUseCase.ejecutar(solicitud));
    }
}
