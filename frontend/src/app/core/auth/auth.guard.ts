import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { RolUsuario } from '../models/rol-usuario';
import { AuthService } from './auth.service';

/**
 * Exige una sesión activa; si no hay, redirige a /login. Como los guards se
 * evalúan en cada navegación, también al usar el botón "atrás" del navegador,
 * después de cerrar sesión ninguna pantalla protegida vuelve a mostrarse
 * (RF-01.7).
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.estaAutenticado() ? true : router.createUrlTree(['/login']);
};

/**
 * RNF-06: exige sesión activa Y que el rol esté entre los permitidos. Si la
 * persona tiene otro rol, la lleva a su propia pantalla de inicio (RF-01.3) en
 * vez de al login, para no sugerir que debe volver a iniciar sesión.
 */
export function roleGuard(rolesPermitidos: RolUsuario[]): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.estaAutenticado()) {
      return router.createUrlTree(['/login']);
    }
    if (!authService.tieneAlgunRol(rolesPermitidos)) {
      return router.createUrlTree([authService.rutaInicio()]);
    }
    return true;
  };
}

/**
 * Para pantallas de visitante (login, registro, recuperar contraseña): quien
 * ya inició sesión va directo a su pantalla de inicio.
 */
export const invitadoGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.estaAutenticado() ? router.createUrlTree([authService.rutaInicio()]) : true;
};
