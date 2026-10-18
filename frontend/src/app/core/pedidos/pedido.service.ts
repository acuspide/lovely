import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { conErrorLegible } from '../http/descarga-archivo';
import { Pedido, ResumenPedidoCliente, SolicitudPedido } from './pedido.model';

/** F-05, F-07 y F-08: pedidos de la clienta con sesión. */
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

  /** RF-07.8: "Mis pedidos", del más reciente al más antiguo. */
  listarMios(): Observable<ResumenPedidoCliente[]> {
    return this.http.get<ResumenPedidoCliente[]>(`${API_BASE_URL}/pedidos`);
  }

  /** RF-07.5 / RF-07.6: cancela un pedido propio Pendiente; el stock vuelve al inventario. */
  cancelar(id: number, motivo: string): Observable<Pedido> {
    return this.http.post<Pedido>(`${API_BASE_URL}/pedidos/${id}/cancelacion`, { motivo });
  }

  /** RF-08.4: factura en PDF de un pedido propio (si faltaba, el backend la emite). */
  descargarFactura(id: number): Observable<Blob> {
    return conErrorLegible(this.http.get(`${API_BASE_URL}/pedidos/${id}/factura`, { responseType: 'blob' }));
  }
}
