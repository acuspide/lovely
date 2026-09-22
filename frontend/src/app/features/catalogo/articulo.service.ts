import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../core/config/api.config';
import { Articulo, GuardarArticuloPayload } from '../../core/models/articulo.model';
import { CategoriaArticulo } from '../../core/models/categoria-articulo';

export interface FiltrosCatalogo {
  categoria?: CategoriaArticulo;
  buscar?: string;
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

    return this.http.get<Articulo[]>(`${API_BASE_URL}/articulos`, { params });
  }

  obtener(id: number): Observable<Articulo> {
    return this.http.get<Articulo>(`${API_BASE_URL}/articulos/${id}`);
  }

  crear(payload: GuardarArticuloPayload): Observable<Articulo> {
    return this.http.post<Articulo>(`${API_BASE_URL}/articulos`, payload);
  }

  actualizar(id: number, payload: GuardarArticuloPayload): Observable<Articulo> {
    return this.http.put<Articulo>(`${API_BASE_URL}/articulos/${id}`, payload);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/articulos/${id}`);
  }
}
