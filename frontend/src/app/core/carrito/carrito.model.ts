import { CategoriaArticulo } from '../models/categoria-articulo';

/** Lo que el navegador guarda de cada producto del carrito: solo producto y cantidad. */
export interface LineaCarrito {
  articuloId: number;
  cantidad: number;
}

/** F-04: estado de una línea revisada contra el inventario (ver EstadoLineaCarrito en el backend). */
export type EstadoLineaCarrito = 'DISPONIBLE' | 'AJUSTADA' | 'AGOTADO' | 'NO_DISPONIBLE';

/** Línea del carrito con los datos actuales del producto, según el backend. */
export interface LineaRevisada {
  articuloId: number;
  nombre: string;
  imagenUrl: string | null;
  categoria: CategoriaArticulo | null;
  marca: string | null;
  precio: number;
  stockDisponible: number;
  cantidadSolicitada: number;
  /** Unidades que sí se pueden comprar (0 si está agotado o no disponible). */
  cantidad: number;
  subtotal: number;
  estado: EstadoLineaCarrito;
  /** Aviso para la clienta cuando algo cambió (CU-02, A1-A3); null si todo está bien. */
  mensaje: string | null;
}

/** Respuesta de POST /api/carrito/revision. */
export interface RevisionCarrito {
  lineas: LineaRevisada[];
  total: number;
  unidades: number;
  requiereAtencion: boolean;
  tieneProductosComprables: boolean;
}

/** Resultado de agregar o cambiar la cantidad de un producto. */
export interface ResultadoCambioCarrito {
  /** false si el producto no se pudo agregar (agotado o no disponible). */
  aplicado: boolean;
  /** Cantidad con la que quedó el producto en el carrito. */
  cantidad: number;
  /** Aviso para la clienta (p. ej. "Solo quedan 2 unidades…"), o null. */
  mensaje: string | null;
  linea: LineaRevisada;
}
