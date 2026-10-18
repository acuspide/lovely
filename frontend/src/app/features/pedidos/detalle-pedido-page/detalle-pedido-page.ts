import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EMPTY, catchError, map, switchMap, tap } from 'rxjs';

import { AuthService } from '../../../core/auth/auth.service';
import { OPCIONES_ENTREGA } from '../../../core/carrito/entrega.model';
import { guardarArchivo } from '../../../core/http/descarga-archivo';
import { extraerMensajeError } from '../../../core/http/mensaje-error';
import { GestionPedidosService } from '../../../core/pedidos/gestion-pedidos.service';
import {
  EstadoPedido,
  PedidoGestion,
  etiquetaEstadoPedido,
  nombreArchivoFactura,
  textoAccionAvanzar,
} from '../../../core/pedidos/pedido.model';
import { BarraSuperior } from '../../../shared/components/barra-superior/barra-superior';
import { DialogoCancelarPedido } from '../../../shared/components/dialogo-cancelar-pedido/dialogo-cancelar-pedido';
import { DialogoConfirmacion } from '../../../shared/components/dialogo-confirmacion/dialogo-confirmacion';
import { InsigniaEstadoPedido } from '../../../shared/components/insignia-estado-pedido/insignia-estado-pedido';

/**
 * RF-07.2 a RF-07.7: detalle de un pedido para el personal (/pedidos/:id).
 *
 * - RF-07.2: productos, cantidades, precios, total, contacto y entrega.
 * - RF-07.3 / RF-07.4: un solo botón con el siguiente estado del diagrama;
 *   nunca se ofrece volver atrás. Se pide confirmación antes de cambiarlo.
 * - RF-07.5 / RF-07.6: "Cancelar pedido" (con motivo) solo para la
 *   Administradora y solo en Pendiente o Confirmado; el stock vuelve.
 * - RF-07.7: historial con fecha, estado anterior, nuevo, quién y motivo.
 * - RF-08.5: la Administradora descarga la factura en PDF (anulada si el
 *   pedido se canceló, RF-08.6).
 *
 * Si otra persona cambió el pedido mientras tanto, el backend responde 409 y
 * la pantalla recarga el pedido para mostrar su estado real.
 */
@Component({
  selector: 'app-detalle-pedido-page',
  imports: [DecimalPipe, RouterLink, BarraSuperior, DialogoConfirmacion, DialogoCancelarPedido, InsigniaEstadoPedido],
  templateUrl: './detalle-pedido-page.html',
  styleUrl: './detalle-pedido-page.scss',
})
export class DetallePedidoPage {
  private readonly gestionPedidos = inject(GestionPedidosService);
  private readonly usuario = inject(AuthService).usuarioActual;
  private readonly esAdministradora = computed(() => this.usuario()?.rol === 'ADMINISTRADORA');

  protected readonly pedido = signal<PedidoGestion | null>(null);
  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);

  protected readonly confirmandoAvance = signal(false);
  protected readonly avanzando = signal(false);
  protected readonly cancelacionAbierta = signal(false);
  protected readonly cancelando = signal(false);
  protected readonly errorCancelacion = signal<string | null>(null);

  /** RF-07.5: la Asesora no cancela; la Administradora solo en Pendiente o Confirmado. */
  protected readonly puedeCancelar = computed(() => this.esAdministradora() && !!this.pedido()?.admiteCancelacion);

  /** RF-08.5: el SyRS da la descarga de facturas a la Administradora (no a la Asesora). */
  protected readonly puedeDescargarFactura = computed(
    () => this.esAdministradora() && !!this.pedido()?.facturaDisponible,
  );
  protected readonly descargandoFactura = signal(false);

  protected readonly textoAvanzar = computed(() => {
    const p = this.pedido();
    return p?.siguienteEstado ? textoAccionAvanzar(p.siguienteEstado, p.entrega.tipo) : '';
  });

  constructor() {
    inject(ActivatedRoute)
      .paramMap.pipe(
        map((parametros) => Number(parametros.get('id'))),
        tap(() => this.cargando.set(true)),
        switchMap((id) =>
          this.gestionPedidos.obtener(id).pipe(
            catchError((err: unknown) => {
              this.cargando.set(false);
              this.errorCarga.set(
                err instanceof HttpErrorResponse && (err.status === 404 || err.status === 400)
                  ? 'No encontramos ese pedido.'
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

  // ---- RF-07.3: avanzar al siguiente estado ----

  protected pedirAvance(): void {
    this.error.set(null);
    this.exito.set(null);
    this.confirmandoAvance.set(true);
  }

  protected confirmarAvance(): void {
    const p = this.pedido();
    this.confirmandoAvance.set(false);
    if (!p?.siguienteEstado || this.avanzando()) {
      return;
    }
    const destino = p.siguienteEstado;
    this.avanzando.set(true);
    this.gestionPedidos.avanzar(p.id, destino).subscribe({
      next: (actualizado) => {
        this.pedido.set(actualizado);
        this.avanzando.set(false);
        this.exito.set(`El pedido quedó en «${this.etiquetaEstado(destino)}».`);
      },
      error: (err: unknown) => {
        this.avanzando.set(false);
        this.mostrarErrorYRecargarSiCambio(err, 'No se pudo cambiar el estado.');
      },
    });
  }

  // ---- RF-08.5: factura ----

  protected descargarFactura(): void {
    const p = this.pedido();
    if (!p || this.descargandoFactura()) {
      return;
    }
    this.error.set(null);
    this.descargandoFactura.set(true);
    this.gestionPedidos.descargarFactura(p.id).subscribe({
      next: (pdf) => {
        this.descargandoFactura.set(false);
        guardarArchivo(pdf, nombreArchivoFactura(p.factura?.numero ?? null, p.numero));
        // Si la factura se emitió en este momento (pedido anterior a F-08), se muestra su número.
        if (!p.factura) {
          this.gestionPedidos.obtener(p.id).subscribe({ next: (actual) => this.pedido.set(actual) });
        }
      },
      error: (err: unknown) => {
        this.descargandoFactura.set(false);
        this.error.set(extraerMensajeError(err, 'No se pudo descargar la factura.'));
      },
    });
  }

  // ---- RF-07.5 / RF-07.6: cancelar ----

  protected abrirCancelacion(): void {
    this.error.set(null);
    this.exito.set(null);
    this.errorCancelacion.set(null);
    this.cancelacionAbierta.set(true);
  }

  protected cancelar(motivo: string): void {
    const p = this.pedido();
    if (!p || this.cancelando()) {
      return;
    }
    this.cancelando.set(true);
    this.errorCancelacion.set(null);
    this.gestionPedidos.cancelar(p.id, motivo).subscribe({
      next: (actualizado) => {
        this.pedido.set(actualizado);
        this.cancelando.set(false);
        this.cancelacionAbierta.set(false);
        this.exito.set('El pedido quedó cancelado y sus unidades volvieron al inventario.');
      },
      error: (err: unknown) => {
        this.cancelando.set(false);
        if (err instanceof HttpErrorResponse && err.status === 409) {
          this.cancelacionAbierta.set(false);
          this.mostrarErrorYRecargarSiCambio(err, 'No se pudo cancelar el pedido.');
        } else {
          this.errorCancelacion.set(extraerMensajeError(err, 'No se pudo cancelar el pedido.'));
        }
      },
    });
  }

  /** 409: otra persona cambió el pedido; se muestra el motivo y su estado real. */
  private mostrarErrorYRecargarSiCambio(err: unknown, mensajePorDefecto: string): void {
    this.error.set(extraerMensajeError(err, mensajePorDefecto));
    const p = this.pedido();
    if (p && err instanceof HttpErrorResponse && err.status === 409) {
      this.gestionPedidos.obtener(p.id).subscribe({ next: (actual) => this.pedido.set(actual) });
    }
  }

  // ---- presentación ----

  protected etiquetaEstado(estado: EstadoPedido | null): string {
    return estado ? etiquetaEstadoPedido(estado, this.pedido()?.entrega.tipo) : 'Registro';
  }

  protected fecha(iso: string): string {
    return new Date(iso).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' });
  }

  protected etiquetaEntrega(tipo: string): string {
    return OPCIONES_ENTREGA.find((o) => o.valor === tipo)?.etiqueta ?? tipo;
  }
}
