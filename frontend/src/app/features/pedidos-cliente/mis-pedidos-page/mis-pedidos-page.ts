import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { OPCIONES_ENTREGA } from '../../../core/carrito/entrega.model';
import { extraerMensajeError } from '../../../core/http/mensaje-error';
import { ResumenPedidoCliente } from '../../../core/pedidos/pedido.model';
import { PedidoService } from '../../../core/pedidos/pedido.service';
import { BarraSuperior } from '../../../shared/components/barra-superior/barra-superior';
import { DialogoCancelarPedido } from '../../../shared/components/dialogo-cancelar-pedido/dialogo-cancelar-pedido';
import { InsigniaEstadoPedido } from '../../../shared/components/insignia-estado-pedido/insignia-estado-pedido';

/**
 * RF-07.8: "Mis pedidos" (/mis-pedidos). La clienta ve todos sus pedidos con
 * su estado, del más reciente al más antiguo, y puede cancelar los que siguen
 * Pendientes (RF-07.5), con motivo. Al cancelar, el stock vuelve (RF-07.6).
 */
@Component({
  selector: 'app-mis-pedidos-page',
  imports: [DecimalPipe, RouterLink, BarraSuperior, DialogoCancelarPedido, InsigniaEstadoPedido],
  templateUrl: './mis-pedidos-page.html',
  styleUrl: './mis-pedidos-page.scss',
})
export class MisPedidosPage {
  private readonly pedidoService = inject(PedidoService);

  protected readonly pedidos = signal<ResumenPedidoCliente[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);

  protected readonly porCancelar = signal<ResumenPedidoCliente | null>(null);
  protected readonly cancelando = signal(false);
  protected readonly errorCancelacion = signal<string | null>(null);

  constructor() {
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);
    this.pedidoService.listarMios().subscribe({
      next: (pedidos) => {
        this.pedidos.set(pedidos);
        this.cargando.set(false);
      },
      error: (err: unknown) => {
        this.error.set(extraerMensajeError(err, 'No se pudieron cargar tus pedidos.'));
        this.cargando.set(false);
      },
    });
  }

  protected abrirCancelacion(pedido: ResumenPedidoCliente): void {
    this.exito.set(null);
    this.error.set(null);
    this.errorCancelacion.set(null);
    this.porCancelar.set(pedido);
  }

  protected cancelar(motivo: string): void {
    const pedido = this.porCancelar();
    if (!pedido || this.cancelando()) {
      return;
    }
    this.cancelando.set(true);
    this.pedidoService.cancelar(pedido.id, motivo).subscribe({
      next: () => {
        this.cancelando.set(false);
        this.porCancelar.set(null);
        this.exito.set(`Cancelamos tu pedido ${pedido.numero}.`);
        this.cargar();
      },
      error: (err: unknown) => {
        this.cancelando.set(false);
        this.errorCancelacion.set(extraerMensajeError(err, 'No se pudo cancelar el pedido.'));
        // 409: el pedido ya fue confirmado por la tienda; se actualiza la lista.
        this.cargar();
      },
    });
  }

  protected fecha(iso: string): string {
    return new Date(iso).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' });
  }

  protected etiquetaEntrega(tipo: string): string {
    return OPCIONES_ENTREGA.find((o) => o.valor === tipo)?.etiqueta ?? tipo;
  }
}
