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

/** Pedido registrado (respuesta de POST /api/pedidos y GET /api/pedidos/{id}). */
export interface Pedido {
  id: number;
  /** RF-05.2: número visible, p. ej. "LG-000123". */
  numero: string;
  estado: string;
  creadoEn: string;
  total: number;
  unidades: number;
  lineas: LineaPedido[];
  entrega: {
    tipo: TipoEntrega;
    nombreContacto: string;
    telefono: string;
    direccion: string | null;
    ciudad: string | null;
    indicaciones: string | null;
  };
}

/** Nombres para mostrar de los estados del pedido (diagrama de estados del SyRS). */
export const ETIQUETAS_ESTADO_PEDIDO: Record<string, string> = {
  PENDIENTE: 'Pendiente',
  CONFIRMADO: 'Confirmado',
  EN_PROCESO: 'En proceso',
  EN_CAMINO: 'En camino',
  ENTREGADO: 'Entregado',
  COMPLETADO: 'Completado',
  CANCELADO: 'Cancelado',
};
