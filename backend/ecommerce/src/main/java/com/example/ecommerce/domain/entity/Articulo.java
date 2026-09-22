package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.CategoriaRequeridaException;
import com.example.ecommerce.domain.exception.DescripcionRequeridaException;
import com.example.ecommerce.domain.exception.ImagenRequeridaException;
import com.example.ecommerce.domain.exception.PrecioRequeridoException;
import com.example.ecommerce.domain.exception.StockNegativoException;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.NombreArticulo;
import com.example.ecommerce.domain.valueobject.Precio;

import java.util.Objects;

/**
 * Producto del catálogo (RF-02). Los campos son exactamente los que exige el
 * SyRS: nombre, descripción, precio, categoría, imagen y stock; "marca" es
 * opcional (aparece en los mockups pero no es un campo obligatorio de RF-02).
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

    public Articulo(
            long id,
            NombreArticulo nombre,
            String descripcion,
            Precio precio,
            CategoriaArticulo categoria,
            String marca,
            String imagenUrl,
            int stock) {

        validarDescripcion(descripcion);
        validarPrecio(precio);
        validarCategoria(categoria);
        validarImagen(imagenUrl);
        validarStock(stock);

        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.marca = marca;
        this.imagenUrl = imagenUrl;
        this.stock = stock;
    }

    /**
     * Aplica una edición completa (CU-01, flujo "Editar Producto"),
     * revalidando las mismas invariantes que en la creación. El nombre no se
     * valida aquí (ya lo valida su propio value object al construirse) pero
     * la unicidad del nombre es responsabilidad del caso de uso, que sí tiene
     * acceso al repositorio.
     */
    public void actualizar(
            NombreArticulo nombre,
            String descripcion,
            Precio precio,
            CategoriaArticulo categoria,
            String marca,
            String imagenUrl,
            int stock) {

        validarDescripcion(descripcion);
        validarPrecio(precio);
        validarCategoria(categoria);
        validarImagen(imagenUrl);
        validarStock(stock);

        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.marca = marca;
        this.imagenUrl = imagenUrl;
        this.stock = stock;
    }

    /** RN12/RF-03: un producto con stock 0 se considera agotado. Se calcula, no se persiste. */
    public boolean estaAgotado() {
        return stock == 0;
    }

    /**
     * Devuelve una copia con el id asignado por la persistencia (secuencia de
     * Oracle), igual que {@code Usuario.conId(long)} en F-01.
     */
    public Articulo conId(long nuevoId) {
        return new Articulo(nuevoId, nombre, descripcion, precio, categoria, marca, imagenUrl, stock);
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

    private void validarImagen(String imagenUrl) {
        if (imagenUrl == null || imagenUrl.trim().isEmpty()) {
            throw new ImagenRequeridaException();
        }
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
    public String getImagenUrl() { return imagenUrl; }
    public int getStock() { return stock; }

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
