import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { Pedido, SolicitudPedido } from './pedido.model';

/** F-05: pedidos de la clienta con sesión. */
@Injectable({ providedIn: 'root' })
export class PedidoService {
  private readonly http = inject(HttpClient);

  /** RF-05.1 a RF-05.4: registra el pedido. Si la clave ya se usó, el backend devuelve el mismo pedido (RF-05.7). */
  registrar(solicitud: SolicitudPedido): Observable<Pedido> {
    return this.http.post<Pedido>(`${API_BASE_URL}/pedidos`, solicitud);
  }

  /** RF-05.5: consulta un pedido propio. */
  obtener(id: number): Observable<Pedido> {
    return this.http.get<Pedido>(`${API_BASE_URL}/pedidos/${id}`);
  }
}
