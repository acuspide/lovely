package com.example.ecommerce.infrastructure.security;

import com.example.ecommerce.application.usecase.ValidarSesionUseCase;
import com.example.ecommerce.application.usecase.ValidarSesionUseCase.SesionActiva;
import com.example.ecommerce.domain.exception.TokenInvalidoException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * Patrón: Chain of Responsibility — eslabón de la cadena de filtros de Spring
 * Security. Si la petición trae un Bearer token cuya sesión sigue vigente,
 * deja al usuario autenticado en el SecurityContext; si no, continúa sin
 * autenticar y las reglas de SecurityConfig responden 401/403.
 *
 * También informa al frontend, en la cabecera {@value #CABECERA_SEGUNDOS_RESTANTES},
 * cuántos segundos le quedan a la sesión: así el aviso de "Seguir conectado"
 * (RF-01.6) se muestra a tiempo aunque el reloj del equipo no esté en hora.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String CABECERA_SEGUNDOS_RESTANTES = "X-Sesion-Segundos-Restantes";
    private static final String PREFIJO_BEARER = "Bearer ";

    private final ValidarSesionUseCase validarSesionUseCase;

    public JwtAuthenticationFilter(ValidarSesionUseCase validarSesionUseCase) {
        this.validarSesionUseCase = validarSesionUseCase;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");

        if (encabezado != null && encabezado.startsWith(PREFIJO_BEARER)) {
            autenticar(encabezado.substring(PREFIJO_BEARER.length()), response);
        }

        filterChain.doFilter(request, response);
    }

    private void autenticar(String token, HttpServletResponse response) {
        try {
            SesionActiva sesion = validarSesionUseCase.ejecutar(token);

            var autenticacion = new UsernamePasswordAuthenticationToken(
                    new UsuarioAutenticado(sesion.idSesion(), sesion.idUsuario(), sesion.email(),
                            sesion.rol().name(), sesion.vigencia()),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + sesion.rol().name())));

            SecurityContextHolder.getContext().setAuthentication(autenticacion);
            response.setHeader(CABECERA_SEGUNDOS_RESTANTES, String.valueOf(sesion.vigencia().toSeconds()));
        } catch (TokenInvalidoException ex) {
            // Sesión cerrada, vencida o de una cuenta desactivada: se sigue sin
            // autenticar y los endpoints protegidos responderán 401.
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Principal puesto en el SecurityContext. Incluye el id de la sesión para
     * poder cerrarla (RF-01.7) o conservarla al cambiar la contraseña (RF-01.8).
     */
    public record UsuarioAutenticado(String idSesion, long id, String email, String rol, Duration vigenciaSesion) {
    }
}
