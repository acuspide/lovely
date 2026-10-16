package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.DatosEntregaInvalidosException;
import com.example.ecommerce.domain.exception.TelefonoInvalidoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** RF-04.6: datos de contacto y entrega del pedido. */
class DatosEntregaTest {

    @Test
    void unDomicilioExigeDireccionYCiudad() {
        assertThrows(DatosEntregaInvalidosException.class, () -> new DatosEntrega(
                TipoEntrega.DOMICILIO, "Ana Pérez", "300 123 4567", " ", "Armenia", null));
        assertThrows(DatosEntregaInvalidosException.class, () -> new DatosEntrega(
                TipoEntrega.ENVIO, "Ana Pérez", "300 123 4567", "Calle 10 # 5-20", null, null));
    }

    @Test
    void unDomicilioCompletoSeAcepta() {
        DatosEntrega datos = new DatosEntrega(TipoEntrega.DOMICILIO, "  Ana Pérez ", "+57 300-123-4567",
                " Calle 10 # 5-20 ", "Armenia", "Apto 301");

        assertEquals("Ana Pérez", datos.getNombreContacto());
        assertEquals("3001234567", datos.getTelefono().getValor());
        assertEquals("Calle 10 # 5-20", datos.getDireccion());
    }

    @Test
    void elRetiroEnTiendaNoNecesitaDireccionYLaIgnora() {
        DatosEntrega datos = new DatosEntrega(TipoEntrega.RETIRO_EN_TIENDA, "Ana Pérez", "3001234567",
                "Calle 10", "Armenia", null);

        assertNull(datos.getDireccion());
        assertNull(datos.getCiudad());
    }

    @Test
    void siempreExigeTipoNombreYTelefonoValido() {
        assertThrows(DatosEntregaInvalidosException.class, () -> new DatosEntrega(
                null, "Ana", "3001234567", null, null, null));
        assertThrows(DatosEntregaInvalidosException.class, () -> new DatosEntrega(
                TipoEntrega.RETIRO_EN_TIENDA, " ", "3001234567", null, null, null));
        assertThrows(TelefonoInvalidoException.class, () -> new DatosEntrega(
                TipoEntrega.RETIRO_EN_TIENDA, "Ana", "12345", null, null, null));
    }

    @Test
    void rechazaTextosDemasiadoLargos() {
        String larga = "a".repeat(DatosEntrega.MAXIMO_DIRECCION + 1);

        assertThrows(DatosEntregaInvalidosException.class, () -> new DatosEntrega(
                TipoEntrega.DOMICILIO, "Ana", "3001234567", larga, "Armenia", null));
    }
}
