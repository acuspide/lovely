package com.example.ecommerce.infrastructure.security;

import com.example.ecommerce.application.TokenProvider;
import com.example.ecommerce.domain.exception.ReglaDominioException;
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
import java.util.List;

/**
 * Patrón: Chain of Responsibility — este filtro es un eslabón más dentro de
 * la cadena de filtros de Spring Security (Servlet Filter Chain). Si hay un
 * Bearer token válido, deja al usuario autenticado en el SecurityContext y
 * delega al siguiente eslabón; si no, simplemente continúa sin autenticar
 * (las reglas de autorización de SecurityConfig deciden si eso basta o no).
 *
 * No usa UserDetailsService: el JWT ya trae email/rol/id firmados, así que no
 * hace falta ir a la base de datos en cada request (autenticación stateless
 * de verdad) ni acoplar el dominio a tipos de Spring Security.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final TokenProvider tokenProvider;

    public JwtAuthenticationFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");

        if (encabezado != null && encabezado.startsWith(PREFIJO_BEARER)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String token = encabezado.substring(PREFIJO_BEARER.length());
            try {
                TokenProvider.DatosToken datos = tokenProvider.validar(token);

                var autoridad = new SimpleGrantedAuthority("ROLE_" + datos.rol());
                var autenticacion = new UsernamePasswordAuthenticationToken(
                        new UsuarioAutenticado(datos.idUsuario(), datos.email(), datos.rol()),
                        null,
                        List.of(autoridad));

                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (ReglaDominioException ex) {
                // Token inválido/expirado: se sigue sin autenticar; los
                // endpoints protegidos responderán 401/403 según corresponda.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Principal ligero puesto en el SecurityContext: solo los datos que ya
     * venían en el JWT, sin volver a tocar la base de datos por request.
     */
    public record UsuarioAutenticado(long id, String email, String rol) {
    }
}
