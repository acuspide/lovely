import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { RolUsuario } from '../models/rol-usuario';
import { AuthService } from './auth.service';

/** Exige una sesión activa; si no hay, redirige a /login. */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.estaAutenticado()) {
    return true;
  }

  return router.createUrlTree(['/login']);
};

/**
 * Factory de guard: exige sesión activa Y que el rol del usuario esté en la
 * lista permitida (p. ej. roleGuard(['ADMINISTRADORA']) para el panel de
 * usuarios). Si está autenticado pero con otro rol, lo manda al dashboard en
 * vez de al login, para no sugerir que necesita volver a iniciar sesión.
 */
export function roleGuard(rolesPermitidos: RolUsuario[]): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.estaAutenticado()) {
      return router.createUrlTree(['/login']);
    }

    if (!authService.tieneAlgunRol(rolesPermitidos)) {
      return router.createUrlTree(['/dashboard']);
    }

    return true;
  };
}
