package com.example.ecommerce.infrastructure.web.rest;

import com.example.ecommerce.domain.exception.CorreoElectronicoDuplicadoException;
import com.example.ecommerce.domain.exception.CredencialesInvalidasException;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.exception.TokenInvalidoException;
import com.example.ecommerce.domain.exception.UsuarioInactivoException;
import com.example.ecommerce.domain.exception.UsuarioNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centraliza la traducción de excepciones de dominio a respuestas HTTP
 * (SRP: los controladores no repiten este mapeo). Usa ProblemDetail
 * (RFC 7807), el formato estándar de error de Spring para APIs REST.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ProblemDetail credencialesInvalidas(CredencialesInvalidasException ex) {
        return problema(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ProblemDetail tokenInvalido(TokenInvalidoException ex) {
        return problema(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UsuarioInactivoException.class)
    public ProblemDetail usuarioInactivo(UsuarioInactivoException ex) {
        return problema(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail accesoDenegado(AccessDeniedException ex) {
        return problema(HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta acción.");
    }

    @ExceptionHandler(CorreoElectronicoDuplicadoException.class)
    public ProblemDetail correoDuplicado(CorreoElectronicoDuplicadoException ex) {
        return problema(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ProblemDetail usuarioNoEncontrado(UsuarioNoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail datosInvalidos(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Datos inválidos.");
        return problema(HttpStatus.BAD_REQUEST, mensaje);
    }

    // Handler genérico: cualquier otra ReglaDominioException (contraseña
    // requerida, rol requerido, correo inválido, etc.) cae aquí como 400.
    // Spring elige siempre el @ExceptionHandler más específico disponible,
    // así que los casos particulares de arriba tienen prioridad sobre este.
    @ExceptionHandler(ReglaDominioException.class)
    public ProblemDetail reglaDominio(ReglaDominioException ex) {
        return problema(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ProblemDetail problema(HttpStatus estado, String mensaje) {
        return ProblemDetail.forStatusAndDetail(estado, mensaje);
    }
}
