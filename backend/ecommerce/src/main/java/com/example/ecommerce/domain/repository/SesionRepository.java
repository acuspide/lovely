package com.example.ecommerce.domain.repository;

import com.example.ecommerce.domain.entity.Sesion;

import java.time.Instant;
import java.util.Optional;

public interface SesionRepository {
    Optional<Sesion> buscarPorId(String id);
    Sesion guardar(Sesion sesion);

    /** Cierra todas las sesiones abiertas del usuario (desactivación, recuperación de contraseña). */
    void cerrarTodasDeUsuario(long usuarioId, Instant ahora);

    /** Cierra las demás sesiones abiertas del usuario y conserva la actual (cambio de contraseña). */
    void cerrarOtrasDeUsuario(long usuarioId, String idSesionConservada, Instant ahora);
}
