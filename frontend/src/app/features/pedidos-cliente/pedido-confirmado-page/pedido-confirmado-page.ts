import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EMPTY, catchError, map, switchMap, tap } from 'rxjs';

import { OPCIONES_ENTREGA } from '../../../core/carrito/entrega.model';
import { guardarArchivo } from '../../../core/http/descarga-archivo';
import { extraerMensajeError } from '../../../core/http/mensaje-error';
import { EstadoPedido, Pedido, etiquetaEstadoPedido, nombreArchivoFactura } from '../../../core/pedidos/pedido.model';
import { PedidoService } from '../../../core/pedidos/pedido.service';
import { BarraSuperior } from '../../../shared/components/barra-superior/barra-superior';
import { DialogoCancelarPedido } from '../../../shared/components/dialogo-cancelar-pedido/dialogo-cancelar-pedido';
import { InsigniaEstadoPedido } from '../../../shared/components/insignia-estado-pedido/insignia-estado-pedido';

/**
 * RF-05.5: página del pedido (/pedido/:id). Justo después de comprar muestra
 * el agradecimiento y el número de pedido (CU-02 paso 12); también sirve para
 * volver a consultarlo desde el enlace del correo (RF-05.6).
 *
 * F-07: muestra el seguimiento del pedido (RF-07.8) y, mientras siga
 * Pendiente, permite cancelarlo con motivo (RF-07.5). F-08 / RF-08.4: una vez
 * confirmado, permite descargar la factura en PDF.
 *
 * Solo la dueña del pedido puede verlo: el backend responde 404 si es de otra.
 */
@Component({
  selector: 'app-pedido-confirmado-page',
  imports: [DecimalPipe, RouterLink, BarraSuperior, DialogoCancelarPedido, InsigniaEstadoPedido],
  templateUrl: './pedido-confirmado-page.html',
  styleUrl: './pedido-confirmado-page.scss',
})
export class PedidoConfirmadoPage {
  private readonly pedidoService = inject(PedidoService);

  protected readonly pedido = signal<Pedido | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly cancelacionAbierta = signal(false);
  protected readonly cancelando = signal(false);
  protected readonly errorCancelacion = signal<string | null>(null);
  protected readonly avisoCancelado = signal(false);
  protected readonly descargandoFactura = signal(false);
  protected readonly errorFactura = signal<string | null>(null);
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

  protected etiquetaEstado(estado: EstadoPedido | null): string {
    return estado ? etiquetaEstadoPedido(estado, this.pedido()?.entrega.tipo) : '';
  }

  /** RF-08.4: descarga la factura; si el pedido era anterior a la facturación, el backend la emite. */
  protected descargarFactura(): void {
    const actual = this.pedido();
    if (!actual || this.descargandoFactura()) {
      return;
    }
    this.descargandoFactura.set(true);
    this.errorFactura.set(null);
    this.pedidoService.descargarFactura(actual.id).subscribe({
      next: (pdf) => {
        this.descargandoFactura.set(false);
        guardarArchivo(pdf, nombreArchivoFactura(actual.numeroFactura, actual.numero));
      },
      error: (err: unknown) => {
        this.descargandoFactura.set(false);
        this.errorFactura.set(extraerMensajeError(err, 'No se pudo descargar la factura.'));
      },
    });
  }

  /** RF-07.5 / RF-07.6: cancelación por la clienta (solo Pendiente). */
  protected cancelar(motivo: string): void {
    const actual = this.pedido();
    if (!actual || this.cancelando()) {
      return;
    }
    this.cancelando.set(true);
    this.errorCancelacion.set(null);
    this.pedidoService.cancelar(actual.id, motivo).subscribe({
      next: (cancelado) => {
        this.pedido.set(cancelado);
        this.cancelando.set(false);
        this.cancelacionAbierta.set(false);
        this.avisoCancelado.set(true);
      },
      error: (err: unknown) => {
        this.cancelando.set(false);
        this.errorCancelacion.set(extraerMensajeError(err, 'No se pudo cancelar el pedido.'));
        // 409: la tienda ya lo confirmó; se recarga para mostrar el estado real.
        if (err instanceof HttpErrorResponse && err.status === 409) {
          this.pedidoService.obtener(actual.id).subscribe({ next: (p) => this.pedido.set(p) });
        }
      },
    });
  }

  /** Fecha y hora en formato colombiano, en la zona horaria del navegador. */
  protected fecha(iso: string): string {
    return new Date(iso).toLocaleString('es-CO', { dateStyle: 'long', timeStyle: 'short' });
  }

  protected etiquetaEntrega(tipo: string): string {
    return OPCIONES_ENTREGA.find((o) => o.valor === tipo)?.etiqueta ?? tipo;
  }
}
