package com.example.ecommerce.infrastructure.web.rest;

import com.example.ecommerce.application.usecase.AvanzarEstadoPedidoUseCase;
import com.example.ecommerce.application.usecase.CancelarPedidoUseCase;
import com.example.ecommerce.application.usecase.ConsultarFacturaUseCase;
import com.example.ecommerce.application.usecase.DescargarFacturaUseCase;
import com.example.ecommerce.application.usecase.ListarPedidosUseCase;
import com.example.ecommerce.application.usecase.ObtenerPedidoParaGestionUseCase;
import com.example.ecommerce.domain.valueobject.EstadoPedido;
import com.example.ecommerce.domain.valueobject.QuienCancela;
import com.example.ecommerce.infrastructure.security.JwtAuthenticationFilter.UsuarioAutenticado;
import com.example.ecommerce.infrastructure.web.rest.dto.AvanzarEstadoPedidoRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.CancelarPedidoRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.PedidoGestionResponse;
import com.example.ecommerce.infrastructure.web.rest.dto.ResumenPedidoGestionResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * F-07: gestión de pedidos para el personal. La Administradora y la Asesora
 * de ventas ven y avanzan los pedidos; solo la Administradora los cancela
 * (diagrama de estados, Figura 13).
 *
 * <ul>
 *   <li>{@code GET /api/pedidos/gestion?estado=&busqueda=}: lista (RF-07.1).</li>
 *   <li>{@code GET /api/pedidos/gestion/{id}}: detalle con historial (RF-07.2, RF-07.7).</li>
 *   <li>{@code PATCH /api/pedidos/gestion/{id}/estado}: siguiente estado (RF-07.3, RF-07.4).</li>
 *   <li>{@code POST /api/pedidos/gestion/{id}/cancelacion}: cancelar con motivo (RF-07.5, RF-07.6).</li>
 *   <li>{@code GET /api/pedidos/gestion/{id}/factura}: factura en PDF, solo Administradora (RF-08.5).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/pedidos/gestion")
@PreAuthorize("hasAnyRole('ADMINISTRADORA', 'ASESORA_VENTAS')")
public class GestionPedidosRestController {

    private final ListarPedidosUseCase listarPedidosUseCase;
    private final ObtenerPedidoParaGestionUseCase obtenerPedidoParaGestionUseCase;
    private final AvanzarEstadoPedidoUseCase avanzarEstadoPedidoUseCase;
    private final CancelarPedidoUseCase cancelarPedidoUseCase;
    private final ConsultarFacturaUseCase consultarFacturaUseCase;
    private final DescargarFacturaUseCase descargarFacturaUseCase;

    public GestionPedidosRestController(ListarPedidosUseCase listarPedidosUseCase,
                                        ObtenerPedidoParaGestionUseCase obtenerPedidoParaGestionUseCase,
                                        AvanzarEstadoPedidoUseCase avanzarEstadoPedidoUseCase,
                                        CancelarPedidoUseCase cancelarPedidoUseCase,
                                        ConsultarFacturaUseCase consultarFacturaUseCase,
                                        DescargarFacturaUseCase descargarFacturaUseCase) {
        this.listarPedidosUseCase = listarPedidosUseCase;
        this.obtenerPedidoParaGestionUseCase = obtenerPedidoParaGestionUseCase;
        this.avanzarEstadoPedidoUseCase = avanzarEstadoPedidoUseCase;
        this.cancelarPedidoUseCase = cancelarPedidoUseCase;
        this.consultarFacturaUseCase = consultarFacturaUseCase;
        this.descargarFacturaUseCase = descargarFacturaUseCase;
    }

    @GetMapping
    public List<ResumenPedidoGestionResponse> listar(@RequestParam(required = false) EstadoPedido estado,
                                                     @RequestParam(required = false) String busqueda) {
        return listarPedidosUseCase.ejecutar(Optional.ofNullable(estado), busqueda).stream()
                .map(ResumenPedidoGestionResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public PedidoGestionResponse obtener(@PathVariable long id) {
        return PedidoGestionResponse.desde(obtenerPedidoParaGestionUseCase.ejecutar(id),
                consultarFacturaUseCase.ejecutar(id));
    }

    @PatchMapping("/{id}/estado")
    public PedidoGestionResponse avanzar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable long id,
                                         @Valid @RequestBody AvanzarEstadoPedidoRequest request) {
        avanzarEstadoPedidoUseCase.ejecutar(id, request.estado(), usuario.id());
        return obtener(id);
    }

    @PostMapping("/{id}/cancelacion")
    @PreAuthorize("hasRole('ADMINISTRADORA')")
    public PedidoGestionResponse cancelar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable long id,
                                          @Valid @RequestBody CancelarPedidoRequest request) {
        cancelarPedidoUseCase.ejecutar(
                new CancelarPedidoUseCase.Solicitud(id, QuienCancela.ADMINISTRADORA, usuario.id(), request.motivo()));
        return obtener(id);
    }

    @GetMapping("/{id}/factura")
    @PreAuthorize("hasRole('ADMINISTRADORA')")
    public ResponseEntity<byte[]> descargarFactura(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                   @PathVariable long id) {
        return RespuestaPdf.de(descargarFacturaUseCase.ejecutar(
                id, new DescargarFacturaUseCase.Solicitante(usuario.id(), false)));
    }
}
