package com.example.ecommerce.infrastructure.pdf;

import com.example.ecommerce.application.DocumentoFactura;
import com.example.ecommerce.domain.entity.Factura;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.valueobject.DatosEntrega;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.domain.valueobject.TipoEntrega;
import com.example.ecommerce.support.Facturas;
import com.example.ecommerce.support.Pedidos;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF-08.3 / RF-08.6: el adaptador de OpenPDF produce un PDF válido, también para facturas anuladas. */
public class GeneradorPdfFacturaOpenPdfTest {

    private final GeneradorPdfFacturaOpenPdf generador = new GeneradorPdfFacturaOpenPdf();

    @Test
    void generaUnPdf() {
        byte[] pdf = generador.generar(documento(false));

        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
        assertTrue(pdf.length > 1000);
    }

    @Test
    void generaUnPdfAnuladoConEnvioADomicilio() {
        byte[] pdf = generador.generar(documento(true));

        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    private static DocumentoFactura documento(boolean anulada) {
        var entrega = new DatosEntrega(TipoEntrega.DOMICILIO, "Ana Pérez", "3001234567", "Cra 14 # 20-30",
                "Armenia", "Portería");
        Pedido pedido = Pedido.registrar(100L, List.of(
                        new DetallePedido(1L, "Labial Mate Rosé", 2, new Precio(new BigDecimal("35900"))),
                        new DetallePedido(2L, "Crema hidratante con ácido hialurónico", 1,
                                new Precio(new BigDecimal("72500")))),
                entrega, Pedidos.AHORA, Pedidos.claveNueva()).conId(12L);
        Factura factura = Factura.emitir(12L, "Ana Pérez", "ana@correo.com", "3001234567",
                Facturas.NEGOCIO.tasaIva(), Pedidos.AHORA).conId(4L);
        if (anulada) {
            factura.anular(Pedidos.AHORA.plusSeconds(600));
        }
        return new DocumentoFactura(Facturas.NEGOCIO, factura, pedido);
    }
}
