package com.example.ecommerce.infrastructure.web.rest;

import com.example.ecommerce.application.usecase.RevisarCarritoUseCase;
import com.example.ecommerce.application.usecase.RevisarCarritoUseCase.LineaSolicitada;
import com.example.ecommerce.infrastructure.web.rest.dto.RevisarCarritoRequest;
import com.example.ecommerce.infrastructure.web.rest.dto.RevisionCarritoResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * F-04: carrito de compras.
 *
 * <p>{@code POST /api/carrito/revision} es público (CU-02 permite armar el
 * carrito como invitada). Es POST y no GET porque el carrito viaja en el
 * cuerpo de la petición, pero no modifica nada: solo consulta el inventario
 * (CO-01).</p>
 */
@RestController
@RequestMapping("/api/carrito")
public class CarritoRestController {

    private final RevisarCarritoUseCase revisarCarritoUseCase;

    public CarritoRestController(RevisarCarritoUseCase revisarCarritoUseCase) {
        this.revisarCarritoUseCase = revisarCarritoUseCase;
    }

    @PostMapping("/revision")
    public ResponseEntity<RevisionCarritoResponse> revisar(@Valid @RequestBody RevisarCarritoRequest request) {
        var lineas = request.lineas().stream()
                .map(linea -> new LineaSolicitada(linea.articuloId(), linea.cantidad()))
                .toList();
        return ResponseEntity.ok(RevisionCarritoResponse.desde(revisarCarritoUseCase.ejecutar(lineas)));
    }
}
