package com.example.ecommerce.domain.repository;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.valueobject.Email;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {
    Optional<Usuario> obtenerPorId(long id);

    /** F-07: varias cuentas en una sola consulta (nombres de las clientas en la lista de pedidos). */
    List<Usuario> obtenerPorIds(Collection<Long> ids);
    Optional<Usuario> buscarPorEmail(Email email);

    /**
     * RF-01.4: como {@link #buscarPorEmail(Email)}, pero bloquea la cuenta
     * hasta que termine la transacción en curso, para que dos inicios de
     * sesión simultáneos no cuenten los intentos fallidos sobre el mismo valor.
     */
    Optional<Usuario> buscarPorEmailParaActualizar(Email email);
    boolean existePorEmail(Email email);

    /** RF-01.2: cuentas del personal (todo rol distinto de Cliente), ordenadas por nombre. */
    List<Usuario> listarPersonal();

    /** RF-01.2: base de la regla "siempre debe quedar al menos una Administradora activa". */
    long contarAdministradorasActivas();

    /**
     * Persiste el usuario y devuelve la entidad resultante. Si {@code usuario}
     * llega "sin persistir" (id = 0), la implementación debe asignarle un id
     * real (secuencia/identity) y devolver la copia con {@link Usuario#conId(long)}.
     */
    Usuario guardar(Usuario usuario);
}
