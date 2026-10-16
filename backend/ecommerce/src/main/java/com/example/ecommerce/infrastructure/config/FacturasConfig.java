package com.example.ecommerce.infrastructure.config;

import com.example.ecommerce.application.DatosNegocio;
import com.example.ecommerce.application.GeneradorPdfFactura;
import com.example.ecommerce.application.GestorTransacciones;
import com.example.ecommerce.application.usecase.ConsultarFacturaUseCase;
import com.example.ecommerce.application.usecase.DescargarFacturaUseCase;
import com.example.ecommerce.application.usecase.EmisorFacturas;
import com.example.ecommerce.domain.repository.FacturaRepository;
import com.example.ecommerce.domain.repository.PedidoRepository;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.infrastructure.pdf.GeneradorPdfFacturaOpenPdf;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Clock;

/**
 * F-08: arma la facturación con sus adaptadores. Los datos del negocio y la
 * tasa de IVA vienen de application.properties (app.negocio.*, app.facturas.*).
 */
@Configuration
public class FacturasConfig {

    @Bean
    public DatosNegocio datosNegocio(@Value("${app.negocio.razon-social}") String razonSocial,
                                     @Value("${app.negocio.nit}") String nit,
                                     @Value("${app.negocio.direccion:}") String direccion,
                                     @Value("${app.negocio.ciudad:}") String ciudad,
                                     @Value("${app.negocio.telefono:}") String telefono,
                                     @Value("${app.negocio.email:}") String email,
                                     @Value("${app.facturas.tasa-iva}") BigDecimal tasaIva) {
        return new DatosNegocio(razonSocial, nit, direccion, ciudad, telefono, email, tasaIva);
    }

    @Bean
    public GeneradorPdfFactura generadorPdfFactura() {
        return new GeneradorPdfFacturaOpenPdf();
    }

    @Bean
    public EmisorFacturas emisorFacturas(FacturaRepository facturaRepository, UsuarioRepository usuarioRepository,
                                         DatosNegocio datosNegocio, Clock reloj) {
        return new EmisorFacturas(facturaRepository, usuarioRepository, datosNegocio, reloj);
    }

    @Bean
    public DescargarFacturaUseCase descargarFacturaUseCase(PedidoRepository pedidoRepository,
                                                           FacturaRepository facturaRepository,
                                                           EmisorFacturas emisorFacturas,
                                                           GeneradorPdfFactura generadorPdfFactura,
                                                           DatosNegocio datosNegocio,
                                                           GestorTransacciones transacciones) {
        return new DescargarFacturaUseCase(pedidoRepository, facturaRepository, emisorFacturas, generadorPdfFactura,
                datosNegocio, transacciones);
    }

    @Bean
    public ConsultarFacturaUseCase consultarFacturaUseCase(FacturaRepository facturaRepository) {
        return new ConsultarFacturaUseCase(facturaRepository);
    }
}
