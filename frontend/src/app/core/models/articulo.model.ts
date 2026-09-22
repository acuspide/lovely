import { CategoriaArticulo } from './categoria-articulo';

export interface Articulo {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  categoria: CategoriaArticulo;
  marca: string | null;
  imagenUrl: string;
  stock: number;
  agotado: boolean;
}

export interface GuardarArticuloPayload {
  nombre: string;
  descripcion: string;
  precio: number;
  categoria: CategoriaArticulo;
  marca: string | null;
  imagenUrl: string;
  stock: number;
}
