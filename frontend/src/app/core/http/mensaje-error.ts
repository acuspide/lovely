import { HttpErrorResponse } from '@angular/common/http';

/**
 * Extrae el mensaje para la usuaria de un error del backend. La API responde
 * con ProblemDetail (RFC 7807), cuyo texto viene en `detail`.
 */
export function extraerMensajeError(error: unknown, mensajePorDefecto: string): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'No se pudo conectar con el servidor. Revisa tu conexión e intenta de nuevo.';
    }
    if (typeof error.error?.detail === 'string') {
      return error.error.detail;
    }
  }
  return mensajePorDefecto;
}
