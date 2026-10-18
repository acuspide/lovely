import { HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, from, switchMap, throwError } from 'rxjs';

/**
 * F-08: utilidades para descargar archivos (la factura en PDF).
 *
 * Cuando una petición pide la respuesta como `Blob`, Angular entrega también
 * los errores como `Blob`, y entonces {@link extraerMensajeError} no puede leer
 * el `detail` del ProblemDetail. Este operador convierte ese Blob de vuelta a
 * JSON para que los mensajes del backend se muestren igual que en el resto.
 */
export function conErrorLegible<T>(peticion: Observable<T>): Observable<T> {
  return peticion.pipe(
    catchError((err: unknown) => {
      if (!(err instanceof HttpErrorResponse) || !(err.error instanceof Blob)) {
        return throwError(() => err);
      }
      return from(err.error.text()).pipe(
        switchMap((texto) => {
          let cuerpo: unknown = null;
          try {
            cuerpo = JSON.parse(texto);
          } catch {
            // No era JSON: se deja el error sin detalle y se usa el mensaje por defecto.
          }
          return throwError(
            () =>
              new HttpErrorResponse({
                error: cuerpo,
                headers: err.headers,
                status: err.status,
                statusText: err.statusText,
                url: err.url ?? undefined,
              }),
          );
        }),
      );
    }),
  );
}

/** Ofrece el archivo al navegador para guardarlo con el nombre indicado. */
export function guardarArchivo(contenido: Blob, nombreArchivo: string): void {
  const url = URL.createObjectURL(contenido);
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = nombreArchivo;
  enlace.click();
  // Se libera después de que el navegador empiece la descarga.
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
