import { Routes } from '@angular/router';

import { authGuard, roleGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./features/catalogo/catalogo-publico/catalogo-publico-page').then((m) => m.CatalogoPublicoPage),
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login-page/login-page').then((m) => m.LoginPage),
  },
  {
    path: 'registro',
    loadComponent: () => import('./features/auth/registro-page/registro-page').then((m) => m.RegistroPage),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard-page/dashboard-page').then((m) => m.DashboardPage),
  },
  {
    path: 'admin/usuarios',
    canActivate: [roleGuard(['ADMINISTRADORA'])],
    loadComponent: () => import('./features/admin/usuarios/usuarios-page/usuarios-page').then((m) => m.UsuariosPage),
  },
  {
    path: 'admin/articulos',
    canActivate: [roleGuard(['ADMINISTRADORA'])],
    loadComponent: () => import('./features/admin/articulos/articulos-page/articulos-page').then((m) => m.ArticulosPage),
  },
  { path: '**', redirectTo: '' },
];
