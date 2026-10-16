package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.ArticuloAgotadoException;
import com.example.ecommerce.domain.exception.ArticuloInactivoException;
import com.example.ecommerce.domain.exception.CantidadInvalidaException;
import com.example.ecommerce.domain.exception.CategoriaRequeridaException;
import com.example.ecommerce.domain.exception.DescripcionRequeridaException;
import com.example.ecommerce.domain.exception.PrecioRequeridoException;
import com.example.ecommerce.domain.exception.StockInsuficienteException;
import com.example.ecommerce.domain.exception.StockNegativoException;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.NombreArticulo;
import com.example.ecommerce.domain.valueobject.Precio;

import java.util.Objects;

/**
 * Producto del catálogo (RF-02). Los campos son exactamente los que exige el
 * SyRS: nombre, descripción, precio, categoría, imagen y stock. "Marca" es
 * opcional (aparece en los mockups pero no es un campo obligatorio de RF-02),
 * y la imagen también lo es: el CU-01 (curso alterno A4) permite guardar el
 * producto sin imagen si su carga falla. El catálogo muestra entonces un
 * marcador "Sin imagen".
 *
 * <p>RF-02.7: un producto puede estar <b>activo</b> (se muestra en el catálogo
 * y se puede vender) o <b>inactivo</b> (oculto para las clientas, pero se
 * conserva con todo su historial). Desactivar es la alternativa a eliminar
 * cuando el producto ya tiene pedidos (RF-02.9).</p>
 */
public class Articulo {

    private final long id;
    private NombreArticulo nombre;
    private String descripcion;
    private Precio precio;
    private CategoriaArticulo categoria;
    private String marca;
    private String imagenUrl;
    private int stock;
    private boolean activo;

    /** Crea un producto activo (el estado por defecto de todo producto nuevo). */
    public Articulo(
            long id,
            NombreArticulo nombre,
            String descripcion,
            Precio precio,
            CategoriaArticulo categoria,
            String marca,
            String imagenUrl,
            int stock) {
        this(id, nombre, descripcion, precio, categoria, marca, imagenUrl, stock, true);
    }

    /** Reconstruye un producto con su estado (lo usa la persistencia). */
    public Articulo(
            long id,
            NombreArticulo nombre,
            String descripcion,
            Precio precio,
            CategoriaArticulo categoria,
            String marca,
            String imagenUrl,
            int stock,
            boolean activo) {

        validarDescripcion(descripcion);
        validarPrecio(precio);
        validarCategoria(categoria);
        validarStock(stock);

        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.marca = marca;
        this.imagenUrl = normalizarImagen(imagenUrl);
        this.stock = stock;
        this.activo = activo;
    }

    /**
     * Aplica una edición (RF-02.6, CU-01 "Editar Producto"), revalidando las
     * mismas invariantes que en la creación. El nombre no se valida aquí (ya
     * lo valida su propio value object al construirse) y la unicidad del
     * nombre es responsabilidad del caso de uso, que sí tiene acceso al
     * repositorio.
     *
     * <p>El stock no forma parte de la edición: se fija al crear el producto
     * (stock inicial) y después solo lo cambia el módulo de inventario.</p>
     */
    public void actualizar(
            NombreArticulo nombre,
            String descripcion,
            Precio precio,
            CategoriaArticulo categoria,
            String marca,
            String imagenUrl) {

        validarDescripcion(descripcion);
        validarPrecio(precio);
        validarCategoria(categoria);

        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.marca = marca;
        this.imagenUrl = normalizarImagen(imagenUrl);
    }

    /**
     * RF-02.7: oculta el producto del catálogo sin borrarlo. Si ya estaba
     * inactivo no pasa nada (la operación es idempotente).
     */
    public void desactivar() {
        this.activo = false;
    }

    /** RF-02.7: vuelve a mostrar en el catálogo un producto desactivado. */
    public void reactivar() {
        this.activo = true;
    }

    /**
     * RF-03.4 / RN12: un producto con stock 0 está agotado. Se calcula a partir
     * del stock (no se guarda aparte), así que se marca "automáticamente" en
     * cuanto el inventario llega a cero y deja de estarlo al reponerse.
     */
    public boolean estaAgotado() {
        return stock == 0;
    }

    /**
     * F-06 / RF-06.1 y RF-06.3: descuenta las unidades vendidas (CO-02
     * descontarStock). El stock nunca queda negativo (RN20): si no alcanza, no
     * cambia nada y se informa cuántas unidades quedan (RF-06.4). Al llegar a
     * cero, el producto queda agotado automáticamente (RF-06.6 / RN12).
     *
     * @throws CantidadInvalidaException   si la cantidad no es mayor que cero (RN19)
     * @throws ArticuloInactivoException   si el producto está desactivado
     * @throws StockInsuficienteException  si no hay unidades suficientes
     */
    public void descontarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new CantidadInvalidaException();
        }
        if (!activo) {
            throw new ArticuloInactivoException();
        }
        if (cantidad > stock) {
            throw new StockInsuficienteException(nombre.getValor(), stock);
        }
        this.stock -= cantidad;
    }

    /**
     * F-07 / RF-07.6: devuelve al inventario las unidades de un pedido
     * cancelado (lo inverso de {@link #descontarStock(int)}). Se permite aunque
     * el producto esté desactivado: las unidades existen físicamente.
     *
     * @throws CantidadInvalidaException si la cantidad no es mayor que cero
     */
    public void reponerStock(int cantidad) {
        if (cantidad <= 0) {
            throw new CantidadInvalidaException();
        }
        this.stock += cantidad;
    }

    /**
     * RF-03.4 / RN01: regla única de "¿se puede vender este producto?". La usan
     * el carrito (y luego la confirmación del pedido) para no repetir la lógica.
     *
     * @throws ArticuloInactivoException si la Administradora lo desactivó (RF-02.7)
     * @throws ArticuloAgotadoException  si no tiene unidades (RN01, RN12)
     */
    public void verificarDisponibleParaVenta() {
        if (!activo) {
            throw new ArticuloInactivoException();
        }
        if (estaAgotado()) {
            throw new ArticuloAgotadoException();
        }
    }

    /**
     * Devuelve una copia con el id asignado por la persistencia (secuencia de
     * Oracle), igual que {@code Usuario.conId(long)} en F-01.
     */
    public Articulo conId(long nuevoId) {
        return new Articulo(nuevoId, nombre, descripcion, precio, categoria, marca, imagenUrl, stock, activo);
    }

    private void validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new DescripcionRequeridaException();
        }
    }

    private void validarPrecio(Precio precio) {
        if (precio == null) {
            throw new PrecioRequeridoException();
        }
    }

    private void validarCategoria(CategoriaArticulo categoria) {
        if (categoria == null) {
            throw new CategoriaRequeridaException();
        }
    }

    /**
     * RF-02.4 / CU-01 A4: la imagen es opcional (si falla la subida se puede
     * continuar sin ella). Un texto vacío se guarda como "sin imagen" (null).
     */
    private static String normalizarImagen(String imagenUrl) {
        return imagenUrl == null || imagenUrl.isBlank() ? null : imagenUrl.trim();
    }

    private void validarStock(int stock) {
        if (stock < 0) {
            throw new StockNegativoException();
        }
    }

    public long getId() { return id; }
    public NombreArticulo getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public Precio getPrecio() { return precio; }
    public CategoriaArticulo getCategoria() { return categoria; }
    public String getMarca() { return marca; }
    /** URL de la imagen, o null si el producto no tiene imagen. */
    public String getImagenUrl() { return imagenUrl; }
    public int getStock() { return stock; }
    public boolean isActivo() { return activo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Articulo otro)) return false;
        return id == otro.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
