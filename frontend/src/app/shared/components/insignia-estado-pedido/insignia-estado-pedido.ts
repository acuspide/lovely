import { Component, computed, input } from '@angular/core';

import { EstadoPedido, etiquetaEstadoPedido } from '../../../core/pedidos/pedido.model';

/**
 * F-07: estado del pedido como "pastilla" de color, igual en la lista del
 * personal, en el detalle y en "Mis pedidos". El color agrupa los estados:
 * por atender (Pendiente), en curso, terminado y cancelado.
 *
 * @example <app-insignia-estado-pedido [estado]="p.estado" [tipoEntrega]="p.entrega.tipo" />
 */
@Component({
  selector: 'app-insignia-estado-pedido',
  template: `<span [class]="'insignia insignia-' + grupo()">{{ etiqueta() }}</span>`,
  styles: `
    .insignia {
      display: inline-block;
      padding: 0.2rem 0.65rem;
      border-radius: 999px;
      font-size: 0.8rem;
      font-weight: 600;
      white-space: nowrap;
    }
    .insignia-pendiente {
      background: #fff4e0;
      color: #8a5a00;
    }
    .insignia-en-curso {
      background: #e6f0ff;
      color: #1f4f99;
    }
    .insignia-terminado {
      background: var(--color-success-bg);
      color: var(--color-success);
    }
    .insignia-cancelado {
      background: var(--color-danger-bg);
      color: var(--color-danger);
    }
  `,
})
export class InsigniaEstadoPedido {
  readonly estado = input.required<EstadoPedido>();
  /** Con retiro en tienda, "En camino" se muestra como "Listo para recoger". */
  readonly tipoEntrega = input<string | undefined>(undefined);

  protected readonly etiqueta = computed(() => etiquetaEstadoPedido(this.estado(), this.tipoEntrega()));

  protected readonly grupo = computed(() => {
    switch (this.estado()) {
      case 'PENDIENTE':
        return 'pendiente';
      case 'ENTREGADO':
      case 'COMPLETADO':
        return 'terminado';
      case 'CANCELADO':
        return 'cancelado';
      default:
        return 'en-curso';
    }
  });
}
