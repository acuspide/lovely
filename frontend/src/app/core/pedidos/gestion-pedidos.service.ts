import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { EstadoPedido, PedidoGestion, ResumenPedidoGestion } from './pedido.model';

/**
 * F-07: gestión de pedidos del personal (Administradora y Asesora de ventas).
 * La cancelación solo la acepta el backend si la sesión es de la Administradora.
 */
@Injectable({ providedIn: 'root' })
export class GestionPedidosService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_BASE_URL}/pedidos/gestion`;

  /** RF-07.1: lista con filtro por estado y búsqueda por número o clienta. */
  listar(estado: EstadoPedido | null, busqueda: string): Observable<ResumenPedidoGestion[]> {
    let params = new HttpParams();
    if (estado) {
      params = params.set('estado', estado);
    }
    if (busqueda.trim()) {
      params = params.set('busqueda', busqueda.trim());
    }
    return this.http.get<ResumenPedidoGestion[]>(this.base, { params });
  }

  /** RF-07.2: detalle con historial (RF-07.7). */
  obtener(id: number): Observable<PedidoGestion> {
    return this.http.get<PedidoGestion>(`${this.base}/${id}`);
  }

  /** RF-07.3: pasa el pedido al estado indicado, que debe ser el siguiente del diagrama. */
  avanzar(id: number, estado: EstadoPedido): Observable<PedidoGestion> {
    return this.http.patch<PedidoGestion>(`${this.base}/${id}/estado`, { estado });
  }

  /** RF-07.5 / RF-07.6: cancelación con motivo (solo Administradora). */
  cancelar(id: number, motivo: string): Observable<PedidoGestion> {
    return this.http.post<PedidoGestion>(`${this.base}/${id}/cancelacion`, { motivo });
  }
}
