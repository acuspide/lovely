import { CategoriaArticulo } from './categoria-articulo';

export interface Articulo {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  categoria: CategoriaArticulo;
  marca: string | null;
  /** RF-02.4: null si el producto no tiene imagen. */
  imagenUrl: string | null;
  stock: number;
  agotado: boolean;
  /** RF-02.7: false si la Administradora lo desactivó (no se muestra en el catálogo). */
  activo: boolean;
}

/** RF-02.6: datos editables de un producto. El stock no está: lo gestiona el módulo de inventario. */
export interface ActualizarArticuloPayload {
  nombre: string;
  descripcion: string;
  precio: number;
  categoria: CategoriaArticulo;
  marca: string | null;
  /** Opcional (CU-01 A4): URL devuelta por la subida de imagen, o null. */
  imagenUrl: string | null;
}

/** RF-02.1: datos para registrar un producto. El stock solo se indica aquí, como stock inicial. */
export interface CrearArticuloPayload extends ActualizarArticuloPayload {
  stockInicial: number;
}

/** RF-02.4: respuesta de POST /api/articulos/imagenes. */
export interface ImagenSubida {
  url: string;
}
