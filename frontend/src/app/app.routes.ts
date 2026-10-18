import { Routes } from '@angular/router';

import { authGuard, invitadoGuard, roleGuard } from './core/auth/auth.guard';

/**
 * RNF-06: cada ruta protegida declara qué roles pueden entrar. Las rutas
 * marcadas con IU-XX son las pantallas de inicio de cada rol (RF-01.3).
 */
export const routes: Routes = [
  // IU-01: catálogo, público e inicio de la Cliente.
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./features/catalogo/catalogo-publico/catalogo-publico-page').then((m) => m.CatalogoPublicoPage),
  },

  // RF-03.5: detalle de un producto, público como el catálogo.
  {
    path: 'producto/:id',
    loadComponent: () =>
      import('./features/catalogo/detalle-producto/detalle-producto-page').then((m) => m.DetalleProductoPage),
  },

  // F-04 / IU-02: carrito, disponible también sin sesión (RF-04.4).
  {
    path: 'carrito',
    pathMatch: 'full',
    loadComponent: () => import('./features/carrito/carrito-page/carrito-page').then((m) => m.CarritoPage),
  },
  // RF-04.5: confirmar exige sesión de Cliente; sin sesión lleva al login y regresa aquí.
  {
    path: 'carrito/confirmar',
    canActivate: [roleGuard(['CLIENTE'])],
    loadComponent: () =>
      import('./features/carrito/confirmar-pedido-page/confirmar-pedido-page').then((m) => m.ConfirmarPedidoPage),
  },

  // F-05 / RF-05.5: pedido de la clienta (confirmación y consulta).
  {
    path: 'pedido/:id',
    canActivate: [roleGuard(['CLIENTE'])],
    loadComponent: () =>
      import('./features/pedidos-cliente/pedido-confirmado-page/pedido-confirmado-page').then(
        (m) => m.PedidoConfirmadoPage,
      ),
  },

  // F-07 / RF-07.8: "Mis pedidos" de la clienta.
  {
    path: 'mis-pedidos',
    canActivate: [roleGuard(['CLIENTE'])],
    loadComponent: () =>
      import('./features/pedidos-cliente/mis-pedidos-page/mis-pedidos-page').then((m) => m.MisPedidosPage),
  },

  // Pantallas de visitante.
  {
    path: 'login',
    canActivate: [invitadoGuard],
    loadComponent: () => import('./features/auth/login-page/login-page').then((m) => m.LoginPage),
  },
  {
    path: 'registro',
    canActivate: [invitadoGuard],
    loadComponent: () => import('./features/auth/registro-page/registro-page').then((m) => m.RegistroPage),
  },
  {
    path: 'recuperar-contrasena',
    canActivate: [invitadoGuard],
    loadComponent: () =>
      import('./features/auth/recuperar-contrasena-page/recuperar-contrasena-page').then((m) => m.RecuperarContrasenaPage),
  },
  {
    // Sin invitadoGuard: el enlace del correo debe funcionar aunque haya otra sesión abierta.
    path: 'restablecer-contrasena',
    loadComponent: () =>
      import('./features/auth/restablecer-contrasena-page/restablecer-contrasena-page').then(
        (m) => m.RestablecerContrasenaPage,
      ),
  },
  {
    path: 'politica-de-datos',
    loadComponent: () =>
      import('./features/auth/politica-datos-page/politica-datos-page').then((m) => m.PoliticaDatosPage),
  },

  // IU-07: perfil, para los cuatro roles.
  {
    path: 'perfil',
    canActivate: [authGuard],
    loadComponent: () => import('./features/perfil/perfil-page/perfil-page').then((m) => m.PerfilPage),
  },

  // IU-03: dashboard de la Administradora.
  {
    path: 'dashboard',
    canActivate: [roleGuard(['ADMINISTRADORA'])],
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

  // IU-04 / F-07: gestión de pedidos (inicio de la Asesora; también la usa la Administradora).
  {
    path: 'pedidos',
    pathMatch: 'full',
    canActivate: [roleGuard(['ASESORA_VENTAS', 'ADMINISTRADORA'])],
    loadComponent: () => import('./features/pedidos/pedidos-page').then((m) => m.PedidosPage),
  },
  {
    path: 'pedidos/:id',
    canActivate: [roleGuard(['ASESORA_VENTAS', 'ADMINISTRADORA'])],
    loadComponent: () =>
      import('./features/pedidos/detalle-pedido-page/detalle-pedido-page').then((m) => m.DetallePedidoPage),
  },

  // IU-05: control de inventario de la Encargada de Inventario.
  {
    path: 'inventario',
    canActivate: [roleGuard(['ENCARGADA_INVENTARIO'])],
    loadComponent: () => import('./features/inventario/inventario-page').then((m) => m.InventarioPage),
  },

  { path: '**', redirectTo: '' },
];
