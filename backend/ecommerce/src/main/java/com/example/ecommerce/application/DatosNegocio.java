package com.example.ecommerce.application;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * F-08 / RF-08.3: datos del negocio que encabezan la factura y tasa de IVA
 * vigente. Vienen de la configuración (application.properties), no del
 * código, para poder cambiarlos sin recompilar.
 *
 * @param tasaIva por ejemplo 0.19; la factura guarda la tasa del momento en que se emitió
 */
public record DatosNegocio(String razonSocial, String nit, String direccion, String ciudad, String telefono,
                           String email, BigDecimal tasaIva) {

    public DatosNegocio {
        Objects.requireNonNull(razonSocial, "razonSocial");
        Objects.requireNonNull(nit, "nit");
        Objects.requireNonNull(tasaIva, "tasaIva");
    }
}
