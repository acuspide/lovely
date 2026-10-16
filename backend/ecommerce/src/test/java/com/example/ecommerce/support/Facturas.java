package com.example.ecommerce.support;

import com.example.ecommerce.application.DatosNegocio;
import com.example.ecommerce.application.DocumentoFactura;
import com.example.ecommerce.application.GeneradorPdfFactura;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Datos y dobles de prueba para facturas (F-08). */
public final class Facturas {

    public static final DatosNegocio NEGOCIO = new DatosNegocio("Lovely Girl", "900123456-7", "Calle 1 # 2-3",
            "Armenia", "3001112233", "contacto@lovelygirl.com", new BigDecimal("0.19"));

    private Facturas() {
    }

    /** Generador falso: no dibuja nada, solo recuerda qué documentos le pidieron. */
    public static final class GeneradorPdfFalso implements GeneradorPdfFactura {
        public final List<DocumentoFactura> generados = new ArrayList<>();

        @Override
        public byte[] generar(DocumentoFactura documento) {
            generados.add(documento);
            return ("PDF " + documento.factura().getNumero().valor()).getBytes(StandardCharsets.UTF_8);
        }
    }
}
