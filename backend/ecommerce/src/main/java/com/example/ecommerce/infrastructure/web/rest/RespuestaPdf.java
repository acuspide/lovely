package com.example.ecommerce.infrastructure.web.rest;

import com.example.ecommerce.application.usecase.DescargarFacturaUseCase.ArchivoFactura;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

/** F-08: respuesta HTTP de un PDF para descargar (la usan la clienta y la Administradora). */
final class RespuestaPdf {

    private RespuestaPdf() {
    }

    static ResponseEntity<byte[]> de(ArchivoFactura archivo) {
        ContentDisposition adjunto = ContentDisposition.attachment()
                .filename(archivo.nombreArchivo(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, adjunto.toString())
                // La factura tiene datos personales: el navegador no debe guardarla en caché.
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(archivo.contenido());
    }
}
