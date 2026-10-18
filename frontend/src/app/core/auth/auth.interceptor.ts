import { HttpErrorResponse, HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, tap, throwError } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { AuthService } from './auth.service';

/** Cabecera con la que el backend informa cuánto le queda a la sesión (RF-01.6). */
const CABECERA_SEGUNDOS_RESTANTES = 'X-Sesion-Segundos-Restantes';

/**
 * Interceptor funcional para las peticiones al backend:
 *  - añade el Bearer token de la sesión;
 *  - actualiza la vigencia de la sesión con la que informa el servidor, porque
 *    cada petición autenticada cuenta como actividad (RF-01.6);
 *  - si una petición que llevaba token recibe 401, la sesión venció o fue
 *    anulada en el servidor: se cierra la sesión local y se va a /login.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);

  const token = authService.obtenerToken();
  const debeAutenticarse = token !== null && request.url.startsWith(API_BASE_URL) && !request.headers.has('Authorization');
  const peticion = debeAutenticarse ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request;

  return next(peticion).pipe(
    tap((evento) => {
      if (evento instanceof HttpResponse) {
        const segundos = evento.headers.get(CABECERA_SEGUNDOS_RESTANTES);
        if (segundos !== null) {
          authService.registrarVigencia(Number(segundos));
        }
      }
    }),
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && peticion.headers.has('Authorization')) {
        authService.cerrarSesion('expirada');
      }
      return throwError(() => error);
    }),
  );
};
