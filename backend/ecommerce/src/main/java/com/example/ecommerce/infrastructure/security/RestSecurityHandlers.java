package com.example.ecommerce.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Traduce a JSON (ProblemDetail) las dos formas de rechazo que Spring
 * Security resuelve ANTES de llegar a los controladores, por lo que
 * GlobalExceptionHandler (@RestControllerAdvice) nunca las ve:
 *  - Sin credenciales -> 401 (AuthenticationEntryPoint).
 *  - Autenticado pero sin el rol requerido -> 403 (AccessDeniedHandler).
 * Sin esto, Spring Security cae a su comportamiento por defecto (403 para
 * ambos casos), perdiendo la distinción semántica 401 vs 403 de la API REST.
 */
final class RestSecurityHandlers {

    private RestSecurityHandlers() {
    }

    static AuthenticationEntryPoint entryPoint(ObjectMapper objectMapper) {
        return (request, response, authException) -> escribir(
                response, objectMapper, HttpStatus.UNAUTHORIZED, "Debes iniciar sesión para acceder a este recurso.");
    }

    static AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, accessDeniedException) -> escribir(
                response, objectMapper, HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta acción.");
    }

    private static void escribir(HttpServletResponse response, ObjectMapper objectMapper,
                                  HttpStatus estado, String mensaje) throws IOException {
        response.setStatus(estado.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, mensaje);
        objectMapper.writeValue(response.getWriter(), problema);
    }
}
