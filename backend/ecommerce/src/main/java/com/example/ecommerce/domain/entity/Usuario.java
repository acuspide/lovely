package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.ContrasenaRequeridaException;
import com.example.ecommerce.domain.exception.NombreUsuarioRequeridoException;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.exception.RolRequeridoException;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;

import java.util.Objects;

public class Usuario {

    private final long id;
    private String nombre;
    private Email email;
    private String contrasenaHash;
    private RolUsuario rol;
    private boolean activo;

    public Usuario(long id, String nombre, Email email, String contrasenaHash, RolUsuario rol) {
        validarNombre(nombre);
        validarContrasenaHash(contrasenaHash);
        validarRol(rol);

        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.contrasenaHash = contrasenaHash;
        this.rol = rol;
        this.activo = true;
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new NombreUsuarioRequeridoException();
        }
    }

    private void validarContrasenaHash(String contrasenaHash) {
        if (contrasenaHash == null || contrasenaHash.trim().isEmpty()) {
            throw new ContrasenaRequeridaException();
        }
    }

    private void validarRol(RolUsuario rol) {
        if (rol == null) {
            throw new RolRequeridoException();
        }
    }

    public void desactivar() {
        this.activo = false;
    }

    public void activar() {
        this.activo = true;
    }

    /**
     * Devuelve una copia de este usuario con el id asignado por la persistencia
     * (secuencia de Oracle). Los adaptadores de repositorio la usan justo después
     * de insertar un usuario nuevo (que llega con id = 0, "sin persistir"),
     * para devolver al caso de uso la entidad ya con su identidad real.
     */
    public Usuario conId(long nuevoId) {
        Usuario copia = new Usuario(nuevoId, this.nombre, this.email, this.contrasenaHash, this.rol);
        if (!this.activo) {
            copia.desactivar();
        }
        return copia;
    }

    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public Email getEmail() { return email; }
    public String getContrasenaHash() { return contrasenaHash; }
    public RolUsuario getRol() { return rol; }
    public boolean isActivo() { return activo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario otro)) return false;
        return id == otro.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}