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

/**
 * Modelo de persistencia (tabla USUARIOS). A propósito es una clase distinta
 * de la entidad de dominio Usuario: el dominio no debe conocer JPA/Hibernate.
 * La traducción entre ambas la hace UsuarioMapper.
 */
@Entity
@Table(name = "USUARIOS")
public class UsuarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuarios_seq_gen")
    @SequenceGenerator(name = "usuarios_seq_gen", sequenceName = "USUARIOS_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 180, unique = true)
    private String email;

    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String contrasenaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolJpa rol;

    @Column(nullable = false)
    private boolean activo;

    protected UsuarioJpaEntity() {
        // Requerido por JPA.
    }

    public UsuarioJpaEntity(Long id, String nombre, String email, String contrasenaHash,
                             RolJpa rol, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.contrasenaHash = contrasenaHash;
        this.rol = rol;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getContrasenaHash() { return contrasenaHash; }
    public RolJpa getRol() { return rol; }
    public boolean isActivo() { return activo; }

    /**
     * Enum de persistencia independiente de RolUsuario (dominio). Mantenerlos
     * separados evita que un refactor del enum de dominio rompa silenciosamente
     * los valores ya almacenados en la tabla, y viceversa.
     */
    public enum RolJpa {
        CLIENTE, ASESORA_VENTAS, ENCARGADA_INVENTARIO, ADMINISTRADORA
    }
}
