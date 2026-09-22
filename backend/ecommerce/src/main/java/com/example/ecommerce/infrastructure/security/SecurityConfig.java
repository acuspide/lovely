package com.example.ecommerce.infrastructure.security;

import com.example.ecommerce.application.TokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Configuración de Spring Security para una API REST stateless consumida por
 * Angular. No se usa UserDetailsService/AuthenticationManager (ver
 * JwtAuthenticationFilter): el login lo resuelve directamente
 * IniciarSesionUseCase y este filtro solo valida el JWT ya emitido.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.origen-permitido}")
    private String origenPermitido;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenProvider tokenProvider,
                                                     ObjectMapper objectMapper) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API stateless con Bearer token, sin cookies de sesión.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        // Sin estos handlers, Spring Security responde 403 tanto para
                        // "sin credenciales" como para "credenciales insuficientes",
                        // perdiendo la distinción 401 vs 403 (ver RestSecurityHandlers).
                        .authenticationEntryPoint(RestSecurityHandlers.entryPoint(objectMapper))
                        .accessDeniedHandler(RestSecurityHandlers.accessDeniedHandler(objectMapper)))
                .addFilterBefore(new JwtAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(List.of(origenPermitido));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource origen = new UrlBasedCorsConfigurationSource();
        origen.registerCorsConfiguration("/**", configuracion);
        return origen;
    }
}
