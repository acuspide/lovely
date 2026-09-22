package com.example.ecommerce.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Modelo de persistencia de Articulo (tabla ARTICULOS). Igual que
 * UsuarioJpaEntity en F-01: el dominio no conoce JPA, la traducción la hace
 * ArticuloMapper.
 */
@Entity
@Table(name = "ARTICULOS")
public class ArticuloJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "articulos_seq_gen")
    @SequenceGenerator(name = "articulos_seq_gen", sequenceName = "ARTICULOS_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 150, unique = true)
    private String nombre;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaJpa categoria;

    @Column(length = 100)
    private String marca;

    @Column(name = "imagen_url", nullable = false, length = 500)
    private String imagenUrl;

    @Column(nullable = false)
    private int stock;

    protected ArticuloJpaEntity() {
        // Requerido por JPA.
    }

    public ArticuloJpaEntity(Long id, String nombre, String descripcion, BigDecimal precio,
                              CategoriaJpa categoria, String marca, String imagenUrl, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.marca = marca;
        this.imagenUrl = imagenUrl;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public CategoriaJpa getCategoria() { return categoria; }
    public String getMarca() { return marca; }
    public String getImagenUrl() { return imagenUrl; }
    public int getStock() { return stock; }

    /** Enum de persistencia independiente de CategoriaArticulo (dominio), mismo motivo que RolJpa en F-01. */
    public enum CategoriaJpa {
        FACIAL, CORPORAL, CAPILAR, MAQUILLAJE
    }
}
