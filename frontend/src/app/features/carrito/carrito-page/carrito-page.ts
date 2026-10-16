import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { RevisionCarrito } from '../../../core/carrito/carrito.model';
import { CarritoService } from '../../../core/carrito/carrito.service';
import { extraerMensajeError } from '../../../core/http/mensaje-error';
import { ETIQUETAS_CATEGORIA } from '../../../core/models/categoria-articulo';
import { BarraSuperior } from '../../../shared/components/barra-superior/barra-superior';
import { DialogoConfirmacion } from '../../../shared/components/dialogo-confirmacion/dialogo-confirmacion';
import { UrlImagenPipe } from '../../../shared/pipes/url-imagen.pipe';

/**
 * IU-02: carrito de compras (RF-04.3).
 *
 * Al abrirse revisa el carrito con el inventario actual: actualiza precios,
 * ajusta lo que ya no alcanza y quita lo agotado o retirado, avisando a la
 * clienta de cada cambio (CU-02, A1-A3). Desde aquí se cambian cantidades, se
 * eliminan productos, se vacía el carrito y se pasa a confirmar el pedido.
 */
@Component({
  selector: 'app-carrito-page',
  imports: [DecimalPipe, RouterLink, BarraSuperior, DialogoConfirmacion, UrlImagenPipe],
  templateUrl: './carrito-page.html',
  styleUrl: './carrito-page.scss',
})
export class CarritoPage {
  private readonly carritoService = inject(CarritoService);
  private readonly router = inject(Router);

  protected readonly revision = signal<RevisionCarrito | null>(null);
  protected readonly cargando = signal(true);
  protected readonly actualizando = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);
  /** Avisos de lo que cambió al revisar (cantidades ajustadas, agotados, retirados). */
  protected readonly avisos = signal<string[]>([]);
  protected readonly confirmandoVaciar = signal(false);
  protected readonly etiquetasCategoria = ETIQUETAS_CATEGORIA;

  /** Solo las líneas que se pueden comprar (las demás ya se quitaron y se avisaron). */
  protected readonly lineas = computed(() => (this.revision()?.lineas ?? []).filter((l) => l.cantidad > 0));

  constructor() {
    this.revisar();
  }

  /** RF-04.3 / RF-04.2: cambia la cantidad sin pasar del stock disponible. */
  cambiarCantidad(articuloId: number, cantidad: number): void {
    if (cantidad < 1 || this.actualizando() !== null) {
      return;
    }
    this.actualizando.set(articuloId);
    this.carritoService.cambiarCantidad(articuloId, cantidad).subscribe({
      next: (resultado) => {
        if (resultado.mensaje) {
          this.avisos.set([resultado.mensaje]);
        }
        this.revisar(false);
      },
      error: (err: unknown) => {
        this.actualizando.set(null);
        this.error.set(extraerMensajeError(err, 'No se pudo cambiar la cantidad.'));
      },
    });
  }

  eliminar(articuloId: number): void {
    this.carritoService.eliminar(articuloId);
    this.revisar(false);
  }

  vaciar(): void {
    this.confirmandoVaciar.set(false);
    this.carritoService.vaciar();
    this.avisos.set([]);
    this.revisar(false);
  }

  /** "Finalizar compra": si no hay sesión, el guard lleva a iniciar sesión y luego regresa (RF-04.5). */
  finalizarCompra(): void {
    this.router.navigateByUrl('/carrito/confirmar');
  }

  /**
   * Revisa todo el carrito contra el inventario y lo deja coherente.
   * @param conAvisos si se muestran los avisos de cambios (al abrir la página).
   */
  private revisar(conAvisos = true): void {
    if (this.revision() === null) {
      this.cargando.set(true);
    }
    this.error.set(null);
    this.carritoService.revisar().subscribe({
      next: (revision) => {
        this.revision.set(revision);
        this.cargando.set(false);
        this.actualizando.set(null);
        if (conAvisos) {
          this.avisos.set(revision.lineas.map((l) => l.mensaje).filter((m): m is string => m !== null));
        }
      },
      error: (err: unknown) => {
        this.cargando.set(false);
        this.actualizando.set(null);
        this.error.set(extraerMensajeError(err, 'No se pudo revisar el carrito. Intenta de nuevo.'));
      },
    });
  }
}
