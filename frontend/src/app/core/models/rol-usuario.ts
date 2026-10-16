/**
 * Espejo exacto del enum RolUsuario del backend
 * (com.example.ecommerce.domain.valueobject.RolUsuario), definido en el SyRS
 * (Sección 2.5, Características del Usuario).
 */
export type RolUsuario = 'CLIENTE' | 'ASESORA_VENTAS' | 'ENCARGADA_INVENTARIO' | 'ADMINISTRADORA';

export const ETIQUETAS_ROL: Record<RolUsuario, string> = {
  CLIENTE: 'Cliente',
  ASESORA_VENTAS: 'Asesora de Ventas',
  ENCARGADA_INVENTARIO: 'Encargada de Inventario',
  ADMINISTRADORA: 'Administradora',
};

/** RF-01.2: roles que la Administradora puede asignar al personal. */
export const ROLES_PERSONAL: RolUsuario[] = ['ASESORA_VENTAS', 'ENCARGADA_INVENTARIO', 'ADMINISTRADORA'];

/**
 * RF-01.3: pantalla de inicio de cada rol después de iniciar sesión.
 *  - Cliente: catálogo (IU-01).
 *  - Administradora: dashboard (IU-03).
 *  - Asesora de Ventas: gestión de pedidos (IU-04).
 *  - Encargada de Inventario: control de inventario (IU-05).
 */
export const RUTA_INICIO_POR_ROL: Record<RolUsuario, string> = {
  CLIENTE: '/',
  ADMINISTRADORA: '/dashboard',
  ASESORA_VENTAS: '/pedidos',
  ENCARGADA_INVENTARIO: '/inventario',
};
