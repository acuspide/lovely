import { DecimalPipe } from '@angular/common';
import { Component, ElementRef, effect, inject, input, output, signal, untracked, viewChild } from '@angular/core';

import { extraerMensajeError } from '../../../../core/http/mensaje-error';
import { Articulo } from '../../../../core/models/articulo.model';
import { ETIQUETAS_CATEGORIA } from '../../../../core/models/categoria-articulo';
import { UrlImagenPipe } from '../../../../shared/pipes/url-imagen.pipe';
import { ArticuloService } from '../../../catalogo/articulo.service';

/**
 * RF-02.5: ventana con el detalle completo de un producto para la
 * Administradora (incluye los desactivados).
 *
 * Al abrirse pide el producto al backend (`GET /api/articulos/gestion/{id}`)
 * en vez de reutilizar la fila de la tabla, para mostrar siempre los datos
 * vigentes (por ejemplo, el stock que pudo cambiar por una venta).
 */
@Component({
  selector: 'app-detalle-articulo',
  imports: [DecimalPipe, UrlImagenPipe],
  templateUrl: './detalle-articulo.html',
  styleUrl: './detalle-articulo.scss',
})
export class DetalleArticulo {
  private readonly articuloService = inject(ArticuloService);

  /** Id del producto a mostrar; null = ventana cerrada. */
  readonly articuloId = input<number | null>(null);
  readonly cerrar = output<void>();
  /** La Administradora quiere editar el producto que está viendo. */
  readonly editar = output<Articulo>();

  protected readonly articulo = signal<Articulo | null>(null);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly etiquetasCategoria = ETIQUETAS_CATEGORIA;

  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');

  constructor() {
    // El efecto solo debe reaccionar a `articuloId`. La carga va dentro de
    // `untracked`: la petición HTTP lee y actualiza señales de la sesión (el
    // interceptor renueva su vigencia con cada respuesta), y si el efecto las
    // rastreara se volvería a ejecutar con cada respuesta, en un ciclo sin fin.
    effect(() => {
      const id = this.articuloId();
      const elemento = this.dialogo().nativeElement;
      untracked(() => {
        if (id === null) {
          if (elemento.open) elemento.close();
          return;
        }
        if (!elemento.open) elemento.showModal();
        this.cargar(id);
      });
    });
  }

  private cargar(id: number): void {
    this.cargando.set(true);
    this.error.set(null);
    this.articulo.set(null);
    this.articuloService.obtenerParaGestion(id).subscribe({
      next: (articulo) => {
        this.articulo.set(articulo);
        this.cargando.set(false);
      },
      error: (err: unknown) => {
        this.cargando.set(false);
        this.error.set(extraerMensajeError(err, 'No se pudo cargar el producto.'));
      },
    });
  }

  protected alPresionarEscape(evento: Event): void {
    evento.preventDefault();
    this.cerrar.emit();
  }

  protected alHacerClicFuera(evento: MouseEvent): void {
    if (evento.target === this.dialogo().nativeElement) {
      this.cerrar.emit();
    }
  }
}
