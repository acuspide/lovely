package com.example.ecommerce.domain.entity;


import com.example.ecommerce.domain.exception.*;
import com.example.ecommerce.domain.valueobject.*;

import java.util.List;
import java.util.Objects;

public class Articulo {
    private final long id;
    private NombreArticulo nombre;
    private Precio precio;
    private Categoria categoria;
    private Marca marca;
    private Tono tono;
    private List<TipoPiel> tiposPiel;
    private Inventario inventario;
    private FechaVencimiento fechaVencimiento;
    private Tienda tienda;
    private boolean eliminado;
    private boolean publicado;

    public Articulo(
            long id,
            NombreArticulo nombre,
            Precio precio,
            Categoria categoria,
            Marca marca,
            Tono tono,
            List<TipoPiel> tiposPiel,
            Inventario inventario,
            FechaVencimiento fechaVencimiento,
            Tienda tienda){

        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.categoria = categoria;
        this.marca = marca;
        this.tono = tono;
        this.tiposPiel = tiposPiel;
        this.inventario = inventario;
        this.fechaVencimiento = fechaVencimiento;
        this.tienda = tienda;
        this.eliminado = false;
        this.publicado = false;

    }

    public void publicar(){
        validarPuedePublicarse();
        this.publicado = true;
    }

    public long getId() {
        return id;
    }
    public boolean isPublicado() {
        return publicado;
    }

    private void validarPuedePublicarse(){
        if (categoria == null){
            throw new CategoriaRequeridaException();
        }
        if (marca == null) {
            throw new MarcaRequeridaException();
        }

        if (precio == null) {
            throw new PrecioRequeridoException();
        }

        if (inventario == null) {
            throw new CantidadDisponibleRequeridaException();
        }

        if (fechaVencimiento == null) {
            throw new FechaVencimientoRequeridaException();
        }
        if (fechaVencimiento.estaVencida()) {
            throw new ArticuloVencidoException();
        }
        if (tienda == null) {
            throw new TiendaRequeridaException();
        }
    }
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
