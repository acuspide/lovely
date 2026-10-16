import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../core/config/api.config';
import { ActualizarArticuloPayload, Articulo, CrearArticuloPayload, ImagenSubida } from '../../core/models/articulo.model';
import { CategoriaArticulo } from '../../core/models/categoria-articulo';

/** RF-03.6: órdenes que acepta el catálogo público (los agotados siempre van al final). */
export type OrdenCatalogo = 'NOMBRE' | 'PRECIO_MENOR' | 'PRECIO_MAYOR';

export interface FiltrosCatalogo {
  categoria?: CategoriaArticulo;
  buscar?: string;
  orden?: OrdenCatalogo;
}

/**
 * Un solo servicio para el catálogo público y el panel admin: ambos leen el
 * mismo recurso (/api/articulos); solo el panel admin usa crear/actualizar/
 * eliminar, que el backend ya restringe a ADMINISTRADORA por su cuenta.
 */
@Injectable({ providedIn: 'root' })
export class ArticuloService {
  private readonly http = inject(HttpClient);

  listar(filtros: FiltrosCatalogo = {}): Observable<Articulo[]> {
    let params = new HttpParams();
    if (filtros.categoria) params = params.set('categoria', filtros.categoria);
    if (filtros.buscar) params = params.set('buscar', filtros.buscar);
    if (filtros.orden) params = params.set('orden', filtros.orden);

    return this.http.get<Articulo[]>(`${API_BASE_URL}/articulos`, { params });
  }

  /** RF-02.5 / RF-02.7: listado del panel admin, incluye productos desactivados. Solo ADMINISTRADORA. */
  listarParaGestion(filtros: FiltrosCatalogo = {}): Observable<Articulo[]> {
    let params = new HttpParams();
    if (filtros.categoria) params = params.set('categoria', filtros.categoria);
    if (filtros.buscar) params = params.set('buscar', filtros.buscar);

    return this.http.get<Articulo[]>(`${API_BASE_URL}/articulos/gestion`, { params });
  }

  /** RF-02.5: detalle para el panel admin (incluye productos desactivados). Solo ADMINISTRADORA. */
  obtenerParaGestion(id: number): Observable<Articulo> {
    return this.http.get<Articulo>(`${API_BASE_URL}/articulos/gestion/${id}`);
  }

  obtener(id: number): Observable<Articulo> {
    return this.http.get<Articulo>(`${API_BASE_URL}/articulos/${id}`);
  }

  crear(payload: CrearArticuloPayload): Observable<Articulo> {
    return this.http.post<Articulo>(`${API_BASE_URL}/articulos`, payload);
  }

  actualizar(id: number, payload: ActualizarArticuloPayload): Observable<Articulo> {
    return this.http.put<Articulo>(`${API_BASE_URL}/articulos/${id}`, payload);
  }

  /**
   * RF-02.4: sube la foto de un producto y devuelve su URL. Se envía como
   * formulario multipart (campo "imagen"); el navegador pone solo la cabecera
   * Content-Type con su separador, por eso no se indica aquí.
   */
  subirImagen(archivo: File): Observable<ImagenSubida> {
    const formulario = new FormData();
    formulario.append('imagen', archivo);
    return this.http.post<ImagenSubida>(`${API_BASE_URL}/articulos/imagenes`, formulario);
  }

  /** RF-02.9: pregunta, sin borrar nada, si el producto se puede eliminar (no tiene pedidos activos). */
  verificarEliminacion(id: number): Observable<{ puedeEliminarse: boolean }> {
    return this.http.get<{ puedeEliminarse: boolean }>(`${API_BASE_URL}/articulos/gestion/${id}/eliminacion`);
  }

  /** RF-02.7: desactiva (activo = false) o reactiva (activo = true) un producto. */
  cambiarEstado(id: number, activo: boolean): Observable<Articulo> {
    return this.http.patch<Articulo>(`${API_BASE_URL}/articulos/${id}/estado`, { activo });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/articulos/${id}`);
  }
}
