package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.DatosEntregaInvalidosException;

import java.util.Objects;

/**
 * Patrón: Value Object — datos de contacto y entrega de un pedido (RF-04.6,
 * CU-02 pasos 8 y 9).
 *
 * <p>Reglas:</p>
 * <ul>
 *   <li>Siempre: tipo de entrega, nombre de quien recibe y un teléfono
 *       colombiano válido ({@link Telefono}).</li>
 *   <li>Domicilio o envío: además, dirección y ciudad.</li>
 *   <li>Retiro en tienda: la dirección y la ciudad se ignoran.</li>
 * </ul>
 *
 * <p>Lo usa el registro del pedido (F-05) para guardarlo junto al pedido.</p>
 */
public final class DatosEntrega {

    public static final int MAXIMO_NOMBRE = 120;
    public static final int MAXIMO_DIRECCION = 200;
    public static final int MAXIMO_CIUDAD = 80;
    public static final int MAXIMO_INDICACIONES = 300;

    private final TipoEntrega tipo;
    private final String nombreContacto;
    private final Telefono telefono;
    private final String direccion;
    private final String ciudad;
    private final String indicaciones;

    public DatosEntrega(TipoEntrega tipo, String nombreContacto, String telefono,
                        String direccion, String ciudad, String indicaciones) {
        if (tipo == null) {
            throw new DatosEntregaInvalidosException("Elige cómo quieres recibir tu pedido.");
        }
        this.tipo = tipo;
        this.nombreContacto = obligatorio(nombreContacto, "el nombre de quien recibe", MAXIMO_NOMBRE);
        this.telefono = new Telefono(telefono);

        if (tipo.requiereDireccion()) {
            this.direccion = obligatorio(direccion, "la dirección", MAXIMO_DIRECCION);
            this.ciudad = obligatorio(ciudad, "la ciudad", MAXIMO_CIUDAD);
        } else {
            this.direccion = null;
            this.ciudad = null;
        }
        this.indicaciones = opcional(indicaciones, "las indicaciones", MAXIMO_INDICACIONES);
    }

    private static String obligatorio(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new DatosEntregaInvalidosException("Escribe " + campo + ".");
        }
        return opcional(valor, campo, maximo);
    }

    private static String opcional(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new DatosEntregaInvalidosException(
                    "El texto de " + campo + " es demasiado largo (máximo " + maximo + " caracteres).");
        }
        return limpio;
    }

    public TipoEntrega getTipo() { return tipo; }
    public String getNombreContacto() { return nombreContacto; }
    public Telefono getTelefono() { return telefono; }
    /** null en retiro en tienda. */
    public String getDireccion() { return direccion; }
    /** null en retiro en tienda. */
    public String getCiudad() { return ciudad; }
    /** Opcional (p. ej. "apto 301, timbre dañado"). */
    public String getIndicaciones() { return indicaciones; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatosEntrega otro)) return false;
        return tipo == otro.tipo && nombreContacto.equals(otro.nombreContacto)
                && telefono.equals(otro.telefono) && Objects.equals(direccion, otro.direccion)
                && Objects.equals(ciudad, otro.ciudad) && Objects.equals(indicaciones, otro.indicaciones);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, nombreContacto, telefono, direccion, ciudad, indicaciones);
    }
}
