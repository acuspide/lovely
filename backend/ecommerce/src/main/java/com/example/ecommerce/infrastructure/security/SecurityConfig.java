package com.example.ecommerce.infrastructure.security;

import com.example.ecommerce.application.usecase.ValidarSesionUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de Spring Security para una API REST consumida por Angular.
 * No se usa la sesión HTTP de Spring (STATELESS): la sesión de F-01 es propia
 * del dominio y la valida JwtAuthenticationFilter en cada petición.
 *
 * RNF-06: lo que es público se declara aquí; todo lo demás exige sesión, y
 * los permisos por rol se aplican con @PreAuthorize en cada controlador.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] ENDPOINTS_PUBLICOS_DE_AUTENTICACION = {
            "/api/auth/registro",
            "/api/auth/login",
            "/api/auth/recuperar-contrasena",
            "/api/auth/restablecer-contrasena"
    };

    // Lista separada por comas: en desarrollo el puerto por defecto de
    // Angular (4200) a veces está ocupado por otro proceso, así que se
    // admite más de un origen en vez de forzar uno solo fijo.
    @Value("${app.cors.origen-permitido}")
    private String origenesPermitidos;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ValidarSesionUseCase validarSesionUseCase,
                                                   ObjectMapper objectMapper) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API con Bearer token, sin cookies de sesión.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, ENDPOINTS_PUBLICOS_DE_AUTENTICACION).permitAll()
                        // Lecturas del catálogo (RF-03): públicas para que las clientas
                        // naveguen sin sesión. Escrituras exigen @PreAuthorize ADMINISTRADORA
                        // (ver ArticuloRestController), lo que ya requiere estar autenticado.
                        .requestMatchers(HttpMethod.GET, "/api/articulos/**").permitAll()
                        // RF-02.4: las fotos de los productos se ven sin sesión, igual que el catálogo.
                        .requestMatchers(HttpMethod.GET, "/imagenes/**").permitAll()
                        // F-04: revisar el carrito no exige sesión (CU-02 permite comprar como invitada
                        // hasta el momento de confirmar). Solo consulta el inventario, no lo modifica.
                        .requestMatchers(HttpMethod.POST, "/api/carrito/revision").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        // Sin estos handlers, Spring Security responde 403 tanto para
                        // "sin credenciales" como para "credenciales insuficientes",
                        // perdiendo la distinción 401 vs 403 (ver RestSecurityHandlers).
                        .authenticationEntryPoint(RestSecurityHandlers.entryPoint(objectMapper))
                        .accessDeniedHandler(RestSecurityHandlers.accessDeniedHandler(objectMapper)))
                .addFilterBefore(new JwtAuthenticationFilter(validarSesionUseCase),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(Arrays.stream(origenesPermitidos.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList());
        configuracion.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // RF-01.6: el frontend lee esta cabecera para saber cuánto le queda a la sesión.
        configuracion.setExposedHeaders(List.of(JwtAuthenticationFilter.CABECERA_SEGUNDOS_RESTANTES));

        UrlBasedCorsConfigurationSource origen = new UrlBasedCorsConfigurationSource();
        origen.registerCorsConfiguration("/**", configuracion);
        return origen;
    }
}
