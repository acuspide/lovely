package com.example.ecommerce.infrastructure.web.rest;

import com.example.ecommerce.application.AlmacenamientoImagenException;
import com.example.ecommerce.domain.exception.ArticuloAgotadoException;
import com.example.ecommerce.domain.exception.ArticuloConPedidosActivosException;
import com.example.ecommerce.domain.exception.ArticuloInactivoException;
import com.example.ecommerce.domain.exception.PedidoNoEncontradoException;
import com.example.ecommerce.domain.exception.StockInsuficienteException;
import com.example.ecommerce.domain.exception.ArticuloNoEncontradoException;
import com.example.ecommerce.domain.exception.CorreoElectronicoDuplicadoException;
import com.example.ecommerce.domain.exception.CredencialesInvalidasException;
import com.example.ecommerce.domain.exception.CuentaBloqueadaException;
import com.example.ecommerce.domain.exception.NombreArticuloDuplicadoException;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.valueobject.ArchivoImagen;
import com.example.ecommerce.domain.exception.TokenInvalidoException;
import com.example.ecommerce.domain.exception.UltimaAdministradoraActivaException;
import com.example.ecommerce.domain.exception.UsuarioInactivoException;
import com.example.ecommerce.domain.exception.UsuarioNoEncontradoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

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

    // RF-01.4: 423 Locked indica que el recurso (la cuenta) está bloqueado
    // temporalmente; el frontend muestra el mensaje con los minutos restantes.
    @ExceptionHandler(CuentaBloqueadaException.class)
    public ProblemDetail cuentaBloqueada(CuentaBloqueadaException ex) {
        ProblemDetail problema = problema(HttpStatus.LOCKED, ex.getMessage());
        problema.setProperty("minutosRestantes", ex.getMinutosRestantes());
        return problema;
    }

    // RF-01.2: siempre debe quedar al menos una Administradora activa.
    @ExceptionHandler(UltimaAdministradoraActivaException.class)
    public ProblemDetail ultimaAdministradoraActiva(UltimaAdministradoraActivaException ex) {
        return problema(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Red de seguridad ante dos altas simultáneas con el mismo correo: la
    // restricción UNIQUE de la base de datos gana y se responde como un
    // conflicto, sin exponer detalles del esquema.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail conflictoDeDatos(DataIntegrityViolationException ex) {
        return problema(HttpStatus.CONFLICT, "La operación entra en conflicto con datos ya registrados.");
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

    @ExceptionHandler(ArticuloNoEncontradoException.class)
    public ProblemDetail articuloNoEncontrado(ArticuloNoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // RF-02.3 / CU-01 A2: el nombre del producto ya está en uso.
    @ExceptionHandler(NombreArticuloDuplicadoException.class)
    public ProblemDetail nombreArticuloDuplicado(NombreArticuloDuplicadoException ex) {
        return problema(HttpStatus.CONFLICT, ex.getMessage());
    }

    // RN17: no se puede eliminar un producto con pedidos activos.
    @ExceptionHandler(ArticuloConPedidosActivosException.class)
    public ProblemDetail articuloConPedidosActivos(ArticuloConPedidosActivosException ex) {
        // RF-02.9: le indica al frontend que ofrezca "Desactivar" en lugar de eliminar.
        ProblemDetail problema = problema(HttpStatus.CONFLICT, ex.getMessage());
        problema.setProperty("accionSugerida", "DESACTIVAR");
        return problema;
    }

    // RF-02.4: Spring rechaza el archivo antes de leerlo si supera el límite configurado.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail imagenDemasiadoGrande(MaxUploadSizeExceededException ex) {
        return problema(HttpStatus.CONTENT_TOO_LARGE,
                "La imagen pesa demasiado. El tamaño máximo es " + ArchivoImagen.TAMANO_MAXIMO_MB + " MB.");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ProblemDetail imagenFaltante(MissingServletRequestPartException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Selecciona una imagen para subir.");
    }

    // CU-01 A4: falla técnica al guardar la imagen; la interfaz ofrece reintentar o seguir sin imagen.
    @ExceptionHandler(AlmacenamientoImagenException.class)
    public ProblemDetail errorAlmacenandoImagen(AlmacenamientoImagenException ex) {
        return problema(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    // RF-05.4 / RN09: el pedido no se registra porque algo del carrito ya no está disponible.
    // 409 + "REVISAR_CARRITO": el frontend vuelve a revisar el carrito y muestra qué cambió.
    @ExceptionHandler({StockInsuficienteException.class, ArticuloAgotadoException.class,
            ArticuloInactivoException.class})
    public ProblemDetail productoNoDisponible(RuntimeException ex) {
        ProblemDetail problema = problema(HttpStatus.CONFLICT, ex.getMessage());
        problema.setProperty("accionSugerida", "REVISAR_CARRITO");
        return problema;
    }

    // RF-06.5: otra compra tiene bloqueado el producto por más de 5 segundos (mucha demanda).
    // No se registró nada: la clienta puede reintentar sin riesgo de duplicar (RF-05.7).
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ProblemDetail productoOcupado(PessimisticLockingFailureException ex) {
        return problema(HttpStatus.SERVICE_UNAVAILABLE,
                "Hay muchas compras de este producto en este momento. Intenta de nuevo en unos segundos.");
    }

    @ExceptionHandler(PedidoNoEncontradoException.class)
    public ProblemDetail pedidoNoEncontrado(PedidoNoEncontradoException ex) {
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

    // p.ej. ?categoria=NO_EXISTE en GET /api/articulos.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return problema(HttpStatus.BAD_REQUEST, "El parámetro '" + ex.getName() + "' no tiene un valor válido.");
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
