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
