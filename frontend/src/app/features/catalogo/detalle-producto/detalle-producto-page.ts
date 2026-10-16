import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EMPTY, catchError, map, switchMap, tap } from 'rxjs';

import { extraerMensajeError } from '../../../core/http/mensaje-error';
import { Articulo } from '../../../core/models/articulo.model';
import { ETIQUETAS_CATEGORIA } from '../../../core/models/categoria-articulo';
import { BarraSuperior } from '../../../shared/components/barra-superior/barra-superior';
import { UrlImagenPipe } from '../../../shared/pipes/url-imagen.pipe';
import { ArticuloService } from '../articulo.service';

/**
 * RF-03.5: página de detalle de un producto del catálogo (/producto/:id),
 * pública como el catálogo.
 *
 * Muestra la foto grande, la descripción completa, el precio y la
 * disponibilidad (RF-03.4). Si el producto no existe o fue desactivado
 * (RF-02.7), el backend responde 404 y se muestra un aviso con el camino de
 * vuelta al catálogo.
 *
 * El botón "Agregar al carrito" llegará con F-04 (RF-04).
 */
@Component({
  selector: 'app-detalle-producto-page',
  imports: [DecimalPipe, RouterLink, BarraSuperior, UrlImagenPipe],
  templateUrl: './detalle-producto-page.html',
  styleUrl: './detalle-producto-page.scss',
})
export class DetalleProductoPage {
  private readonly ruta = inject(ActivatedRoute);
  private readonly articuloService = inject(ArticuloService);

  protected readonly articulo = signal<Articulo | null>(null);
  protected readonly cargando = signal(true);
  protected readonly noEncontrado = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly etiquetasCategoria = ETIQUETAS_CATEGORIA;

  constructor() {
    // switchMap: si se navega de un producto a otro, se cancela la petición anterior.
    this.ruta.paramMap
      .pipe(
        map((parametros) => Number(parametros.get('id'))),
        tap(() => this.reiniciar()),
        // El error se maneja dentro del switchMap para que un 404 no corte la
        // suscripción: si luego se navega a otro producto, debe volver a cargar.
        switchMap((id) =>
          this.articuloService.obtener(id).pipe(
            catchError((err: unknown) => {
              this.mostrarError(err);
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((articulo) => {
        this.articulo.set(articulo);
        this.cargando.set(false);
      });
  }

  private mostrarError(err: unknown): void {
    this.cargando.set(false);
    // 404: no existe o está desactivado; 400: el id de la URL no es un número.
    if (err instanceof HttpErrorResponse && (err.status === 404 || err.status === 400)) {
      this.noEncontrado.set(true);
    } else {
      this.error.set(extraerMensajeError(err, 'No se pudo cargar el producto. Intenta de nuevo.'));
    }
  }

  private reiniciar(): void {
    this.cargando.set(true);
    this.noEncontrado.set(false);
    this.error.set(null);
    this.articulo.set(null);
  }
}
