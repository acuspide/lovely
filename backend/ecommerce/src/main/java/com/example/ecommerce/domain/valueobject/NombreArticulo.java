package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.NombreArticuloRequeridoException;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Patrón: Value Object — nombre comercial de un producto del catálogo.
 *
 * <p>RF-02.3 / RN17: no pueden existir dos productos con el mismo nombre. Para
 * la tienda, "Crema Hidratante", "crema hidratante" y "Crema  Hidratánte" son
 * el mismo producto, así que la comparación no se hace sobre el texto tal cual
 * sino sobre su {@link #clave() clave de comparación}.</p>
 *
 * <ul>
 *   <li>{@link #getValor()}: el nombre como se muestra en el catálogo (sin
 *       espacios sobrantes, pero respetando mayúsculas y tildes).</li>
 *   <li>{@link #clave()}: el nombre en minúsculas, sin tildes y con un solo
 *       espacio entre palabras. Es lo que se usa para detectar duplicados y
 *       lo que la base de datos protege con una restricción UNIQUE.</li>
 * </ul>
 */
public class NombreArticulo {

    private static final Pattern ESPACIOS_REPETIDOS = Pattern.compile("\\s+");
    private static final Pattern MARCAS_DIACRITICAS = Pattern.compile("\\p{M}");

    private final String valor;
    private final String clave;

    public NombreArticulo(String valor) {
        validarNombre(valor);
        this.valor = limpiarEspacios(valor);
        this.clave = normalizar(this.valor);
    }

    private void validarNombre(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new NombreArticuloRequeridoException();
        }
    }

    /** Quita espacios al inicio y al final y deja uno solo entre palabras. */
    private static String limpiarEspacios(String texto) {
        return ESPACIOS_REPETIDOS.matcher(texto.trim()).replaceAll(" ");
    }

    /**
     * Lleva cualquier texto a la forma de comparación de los nombres: sin
     * espacios sobrantes, sin tildes y en minúsculas. Separa cada letra de su
     * tilde (forma NFD: "á" pasa a "a" + "´") y elimina las tildes. Así
     * "Ñandú" y "nandu" quedan iguales.
     *
     * <p>Lo usan la clave de unicidad (RF-02.3) y la búsqueda por nombre
     * (RF-02.5), para que ambas traten igual mayúsculas y tildes.</p>
     */
    public static String normalizar(String texto) {
        String sinTildes = MARCAS_DIACRITICAS
                .matcher(Normalizer.normalize(limpiarEspacios(texto), Normalizer.Form.NFD))
                .replaceAll("");
        return sinTildes.toLowerCase(Locale.ROOT);
    }

    public String getValor() {
        return valor;
    }

    /** Clave de comparación usada para garantizar nombres únicos (RF-02.3). */
    public String clave() {
        return clave;
    }

    /** Dos nombres son iguales si tienen la misma clave, aunque se escriban distinto. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NombreArticulo otro)) return false;
        return clave.equals(otro.clave);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clave);
    }

    @Override
    public String toString() {
        return valor;
    }
}
