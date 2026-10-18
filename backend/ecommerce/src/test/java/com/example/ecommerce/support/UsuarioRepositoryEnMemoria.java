package com.example.ecommerce.support;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Doble de pruebas del puerto UsuarioRepository. Guarda y devuelve copias,
 * igual que una base de datos real: si un caso de uso modifica una cuenta y
 * olvida guardarla, la prueba lo detecta.
 */
public class UsuarioRepositoryEnMemoria implements UsuarioRepository {

    private final Map<Long, Usuario> usuarios = new HashMap<>();
    private final AtomicLong secuenciaId = new AtomicLong(0);

    @Override
    public Optional<Usuario> obtenerPorId(long id) {
        return Optional.ofNullable(usuarios.get(id)).map(UsuarioRepositoryEnMemoria::copia);
    }

    @Override
    public List<Usuario> obtenerPorIds(Collection<Long> ids) {
        return ids.stream().distinct().map(this::obtenerPorId).flatMap(Optional::stream).toList();
    }

    @Override
    public Optional<Usuario> buscarPorEmail(Email email) {
        return usuarios.values().stream()
                .filter(usuario -> usuario.getEmail().equals(email))
                .findFirst()
                .map(UsuarioRepositoryEnMemoria::copia);
    }

    /** En memoria no hay concurrencia que controlar: equivale a buscarPorEmail. */
    @Override
    public Optional<Usuario> buscarPorEmailParaActualizar(Email email) {
        return buscarPorEmail(email);
    }

    @Override
    public boolean existePorEmail(Email email) {
        return usuarios.values().stream().anyMatch(usuario -> usuario.getEmail().equals(email));
    }

    @Override
    public List<Usuario> listarPersonal() {
        return usuarios.values().stream()
                .filter(usuario -> usuario.getRol().esPersonal())
                .sorted(Comparator.comparing(Usuario::getNombre))
                .map(UsuarioRepositoryEnMemoria::copia)
                .toList();
    }

    @Override
    public long contarAdministradorasActivas() {
        return usuarios.values().stream()
                .filter(usuario -> usuario.isActivo() && usuario.getRol() == RolUsuario.ADMINISTRADORA)
                .count();
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        long id = usuario.getId() == Usuario.SIN_PERSISTIR ? secuenciaId.incrementAndGet() : usuario.getId();
        Usuario guardado = usuario.conId(id);
        usuarios.put(id, guardado);
        return copia(guardado);
    }

    private static Usuario copia(Usuario usuario) {
        return usuario.conId(usuario.getId());
    }
}
