import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EMPTY, catchError, map, switchMap, tap } from 'rxjs';

import { OPCIONES_ENTREGA } from '../../../core/carrito/entrega.model';
import { extraerMensajeError } from '../../../core/http/mensaje-error';
import { ETIQUETAS_ESTADO_PEDIDO, Pedido } from '../../../core/pedidos/pedido.model';
import { PedidoService } from '../../../core/pedidos/pedido.service';
import { BarraSuperior } from '../../../shared/components/barra-superior/barra-superior';

/**
 * RF-05.5: página del pedido (/pedido/:id). Justo después de comprar muestra
 * el agradecimiento y el número de pedido (CU-02 paso 12); también sirve para
 * volver a consultarlo desde el enlace del correo (RF-05.6).
 *
 * Solo la dueña del pedido puede verlo: el backend responde 404 si es de otra.
 */
@Component({
  selector: 'app-pedido-confirmado-page',
  imports: [DecimalPipe, RouterLink, BarraSuperior],
  templateUrl: './pedido-confirmado-page.html',
  styleUrl: './pedido-confirmado-page.scss',
})
export class PedidoConfirmadoPage {
  private readonly pedidoService = inject(PedidoService);

  protected readonly pedido = signal<Pedido | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  /** true si se llegó aquí recién confirmando la compra (muestra el agradecimiento). */
  protected readonly recienCreado = inject(Router).getCurrentNavigation()?.extras.state?.['recienCreado'] === true;

  constructor() {
    inject(ActivatedRoute)
      .paramMap.pipe(
        map((parametros) => Number(parametros.get('id'))),
        tap(() => this.cargando.set(true)),
        switchMap((id) =>
          this.pedidoService.obtener(id).pipe(
            catchError((err: unknown) => {
              this.cargando.set(false);
              this.error.set(
                err instanceof HttpErrorResponse && (err.status === 404 || err.status === 400)
                  ? 'No encontramos ese pedido en tu cuenta.'
                  : extraerMensajeError(err, 'No se pudo cargar el pedido.'),
              );
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((pedido) => {
        this.pedido.set(pedido);
        this.cargando.set(false);
      });
  }

  protected etiquetaEstado(estado: string): string {
    return ETIQUETAS_ESTADO_PEDIDO[estado] ?? estado;
  }

  /** Fecha y hora en formato colombiano, en la zona horaria del navegador. */
  protected fecha(iso: string): string {
    return new Date(iso).toLocaleString('es-CO', { dateStyle: 'long', timeStyle: 'short' });
  }

  protected etiquetaEntrega(tipo: string): string {
    return OPCIONES_ENTREGA.find((o) => o.valor === tipo)?.etiqueta ?? tipo;
  }
}
