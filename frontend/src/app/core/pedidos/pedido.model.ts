import { LineaCarrito } from '../carrito/carrito.model';
import { DatosEntrega, TipoEntrega } from '../carrito/entrega.model';

/** F-05: cuerpo de POST /api/pedidos. */
export interface SolicitudPedido {
  /** RF-05.7: identificador único del intento de compra; se reutiliza al reintentar. */
  claveIdempotencia: string;
  lineas: LineaCarrito[];
  entrega: DatosEntrega;
}

export interface LineaPedido {
  articuloId: number;
  nombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

/** F-07: estados del diagrama del SyRS (Figura 13). */
export type EstadoPedido =
  | 'PENDIENTE'
  | 'CONFIRMADO'
  | 'EN_PROCESO'
  | 'EN_CAMINO'
  | 'ENTREGADO'
  | 'COMPLETADO'
  | 'CANCELADO';

/** Datos de entrega tal como los devuelve la API. */
export interface EntregaPedido {
  tipo: TipoEntrega;
  nombreContacto: string;
  telefono: string;
  direccion: string | null;
  ciudad: string | null;
  indicaciones: string | null;
}

/** RF-07.8: un paso del seguimiento que ve la clienta. */
export interface CambioEstadoCliente {
  estadoAnterior: EstadoPedido | null;
  estadoNuevo: EstadoPedido;
  fecha: string;
  motivo: string | null;
}

/** Pedido de la clienta (POST /api/pedidos, GET /api/pedidos/{id}, cancelación). */
export interface Pedido {
  id: number;
  /** RF-05.2: número visible, p. ej. "LG-000123". */
  numero: string;
  estado: EstadoPedido;
  creadoEn: string;
  total: number;
  unidades: number;
  lineas: LineaPedido[];
  entrega: EntregaPedido;
  /** RF-07.5: la clienta solo puede cancelar mientras está Pendiente. */
  puedeCancelar: boolean;
  motivoCancelacion: string | null;
  historial: CambioEstadoCliente[];
}

/** RF-07.8: fila de "Mis pedidos" (GET /api/pedidos). */
export interface ResumenPedidoCliente {
  id: number;
  numero: string;
  estado: EstadoPedido;
  tipoEntrega: TipoEntrega;
  creadoEn: string;
  total: number;
  unidades: number;
  puedeCancelar: boolean;
}

/** RF-07.1: fila de la lista del personal (GET /api/pedidos/gestion). */
export interface ResumenPedidoGestion {
  id: number;
  numero: string;
  clienta: string;
  creadoEn: string;
  total: number;
  estado: EstadoPedido;
  tipoEntrega: TipoEntrega;
}

/** RF-07.7: entrada del historial con quién hizo el cambio (solo personal). */
export interface CambioEstadoGestion {
  fecha: string;
  estadoAnterior: EstadoPedido | null;
  estadoNuevo: EstadoPedido;
  usuario: string;
  motivo: string | null;
}

/** RF-07.2: detalle del pedido para el personal (GET /api/pedidos/gestion/{id}). */
export interface PedidoGestion {
  id: number;
  numero: string;
  estado: EstadoPedido;
  /** RF-07.3: único paso que se ofrece; null en estados finales. */
  siguienteEstado: EstadoPedido | null;
  /** RF-07.5: el estado admite cancelación (la pantalla solo la ofrece a la Administradora). */
  admiteCancelacion: boolean;
  creadoEn: string;
  total: number;
  unidades: number;
  clienta: { id: number; nombre: string };
  lineas: LineaPedido[];
  entrega: EntregaPedido;
  motivoCancelacion: string | null;
  historial: CambioEstadoGestion[];
}

/** Orden del flujo normal, para el filtro por estado (RF-07.1). */
export const ESTADOS_PEDIDO: readonly EstadoPedido[] = [
  'PENDIENTE',
  'CONFIRMADO',
  'EN_PROCESO',
  'EN_CAMINO',
  'ENTREGADO',
  'COMPLETADO',
  'CANCELADO',
];

/** RF-07.5: máximo de caracteres del motivo de cancelación (igual que el backend). */
export const MAX_MOTIVO_CANCELACION = 300;

/** Nombres para mostrar de los estados del pedido (diagrama de estados del SyRS). */
export const ETIQUETAS_ESTADO_PEDIDO: Record<EstadoPedido, string> = {
  PENDIENTE: 'Pendiente',
  CONFIRMADO: 'Confirmado',
  EN_PROCESO: 'En proceso',
  EN_CAMINO: 'En camino',
  ENTREGADO: 'Entregado',
  COMPLETADO: 'Completado',
  CANCELADO: 'Cancelado',
};

/**
 * Nombre del estado para mostrar. Decisión de F-07: cuando la clienta recoge
 * en tienda no hay despacho, así que "En camino" se muestra como "Listo para
 * recoger" (el estado guardado es el mismo).
 */
export function etiquetaEstadoPedido(estado: EstadoPedido | string, tipoEntrega?: TipoEntrega | string): string {
  if (estado === 'EN_CAMINO' && tipoEntrega === 'RETIRO_EN_TIENDA') {
    return 'Listo para recoger';
  }
  return ETIQUETAS_ESTADO_PEDIDO[estado as EstadoPedido] ?? estado;
}

/** RF-07.3: texto del botón que lleva el pedido a su siguiente estado. */
export function textoAccionAvanzar(destino: EstadoPedido, tipoEntrega: TipoEntrega | string): string {
  switch (destino) {
    case 'CONFIRMADO':
      return 'Confirmar pedido';
    case 'EN_PROCESO':
      return 'Empezar a preparar';
    case 'EN_CAMINO':
      return tipoEntrega === 'RETIRO_EN_TIENDA' ? 'Marcar listo para recoger' : 'Marcar como despachado';
    case 'ENTREGADO':
      return 'Marcar como entregado';
    case 'COMPLETADO':
      return 'Completar pedido';
    default:
      return 'Pasar a ' + etiquetaEstadoPedido(destino, tipoEntrega);
  }
}
