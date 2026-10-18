import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EMPTY, catchError, combineLatest, debounceTime, distinctUntilChanged, startWith, switchMap, tap } from 'rxjs';

import { OPCIONES_ENTREGA } from '../../core/carrito/entrega.model';
import { extraerMensajeError } from '../../core/http/mensaje-error';
import { GestionPedidosService } from '../../core/pedidos/gestion-pedidos.service';
import {
  ESTADOS_PEDIDO,
  ETIQUETAS_ESTADO_PEDIDO,
  EstadoPedido,
  ResumenPedidoGestion,
} from '../../core/pedidos/pedido.model';
import { BarraSuperior } from '../../shared/components/barra-superior/barra-superior';
import { InsigniaEstadoPedido } from '../../shared/components/insignia-estado-pedido/insignia-estado-pedido';

/**
 * IU-04 / RF-07.1: lista de pedidos para la Asesora de ventas y la
 * Administradora: número, clienta, fecha, total y estado, del más reciente al
 * más antiguo. Se filtra por estado y se busca por número o nombre de la
 * clienta (la búsqueda la resuelve el backend).
 *
 * Al abrir un pedido se va a su detalle (/pedidos/:id), donde se cambia el
 * estado (RF-07.2 a RF-07.7).
 */
@Component({
  selector: 'app-pedidos-page',
  imports: [DecimalPipe, ReactiveFormsModule, RouterLink, BarraSuperior, InsigniaEstadoPedido],
  templateUrl: './pedidos-page.html',
  styleUrl: './pedidos-page.scss',
})
export class PedidosPage {
  private readonly gestionPedidos = inject(GestionPedidosService);

  protected readonly estados = ESTADOS_PEDIDO;
  protected readonly etiquetasEstado = ETIQUETAS_ESTADO_PEDIDO;

  protected readonly buscador = new FormControl('', { nonNullable: true });
  protected readonly estadoFiltro = signal<EstadoPedido | null>(null);
  private readonly busqueda = toSignal(
    this.buscador.valueChanges.pipe(startWith(''), debounceTime(300), distinctUntilChanged()),
    { initialValue: '' },
  );

  protected readonly pedidos = signal<ResumenPedidoGestion[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly hayFiltros = computed(() => this.estadoFiltro() !== null || this.busqueda().trim() !== '');

  constructor() {
    combineLatest([toObservable(this.estadoFiltro), toObservable(this.busqueda)])
      .pipe(
        tap(() => {
          this.cargando.set(true);
          this.error.set(null);
        }),
        switchMap(([estado, busqueda]) =>
          this.gestionPedidos.listar(estado, busqueda).pipe(
            catchError((err: unknown) => {
              this.error.set(extraerMensajeError(err, 'No se pudieron cargar los pedidos.'));
              this.pedidos.set([]);
              this.cargando.set(false);
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((pedidos) => {
        this.pedidos.set(pedidos);
        this.cargando.set(false);
      });
  }

  protected filtrarPorEstado(valor: string): void {
    this.estadoFiltro.set(valor ? (valor as EstadoPedido) : null);
  }

  protected limpiarFiltros(): void {
    this.buscador.setValue('');
    this.estadoFiltro.set(null);
  }

  protected fecha(iso: string): string {
    return new Date(iso).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' });
  }

  protected etiquetaEntrega(tipo: string): string {
    return OPCIONES_ENTREGA.find((o) => o.valor === tipo)?.etiqueta ?? tipo;
  }
}
