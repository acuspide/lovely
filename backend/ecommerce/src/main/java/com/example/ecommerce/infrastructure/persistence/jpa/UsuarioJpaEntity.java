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

import java.time.LocalDateTime;

/**
 * Modelo de persistencia (tabla USUARIOS). A propósito es una clase distinta
 * de la entidad de dominio Usuario: el dominio no debe conocer JPA/Hibernate.
 * La traducción entre ambas la hace UsuarioMapper. Las fechas se guardan en UTC.
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

    @Column(length = 10)
    private String telefono;

    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String contrasenaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolJpa rol;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "habeas_data_aceptado_en")
    private LocalDateTime habeasDataAceptadoEn;

    protected UsuarioJpaEntity() {
        // Requerido por JPA.
    }

    public UsuarioJpaEntity(Long id, String nombre, String email, String telefono, String contrasenaHash,
                            RolJpa rol, boolean activo, int intentosFallidos, LocalDateTime bloqueadoHasta,
                            LocalDateTime habeasDataAceptadoEn) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.contrasenaHash = contrasenaHash;
        this.rol = rol;
        this.activo = activo;
        this.intentosFallidos = intentosFallidos;
        this.bloqueadoHasta = bloqueadoHasta;
        this.habeasDataAceptadoEn = habeasDataAceptadoEn;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getContrasenaHash() { return contrasenaHash; }
    public RolJpa getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public LocalDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public LocalDateTime getHabeasDataAceptadoEn() { return habeasDataAceptadoEn; }

    /**
     * Enum de persistencia independiente de RolUsuario (dominio). Mantenerlos
     * separados evita que un refactor del enum de dominio rompa silenciosamente
     * los valores ya almacenados en la tabla, y viceversa.
     */
    public enum RolJpa {
        CLIENTE, ASESORA_VENTAS, ENCARGADA_INVENTARIO, ADMINISTRADORA
    }
}
