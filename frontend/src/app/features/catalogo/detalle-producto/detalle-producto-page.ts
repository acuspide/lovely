import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EMPTY, catchError, map, switchMap, tap } from 'rxjs';

import { AuthService } from '../../../core/auth/auth.service';
import { CarritoService } from '../../../core/carrito/carrito.service';
import { extraerMensajeError } from '../../../core/http/mensaje-error';
import { Articulo } from '../../../core/models/articulo.model';
import { ETIQUETAS_CATEGORIA } from '../../../core/models/categoria-articulo';
import { AvisoCarrito, AvisoCarritoComponent } from '../../../shared/components/aviso-carrito/aviso-carrito';
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
 * RF-04.1 / RF-04.2: la clienta elige la cantidad (entre 1 y el stock
 * disponible, descontando lo que ya tiene en el carrito) y la agrega.
 */
@Component({
  selector: 'app-detalle-producto-page',
  imports: [DecimalPipe, RouterLink, BarraSuperior, UrlImagenPipe, AvisoCarritoComponent],
  templateUrl: './detalle-producto-page.html',
  styleUrl: './detalle-producto-page.scss',
})
export class DetalleProductoPage {
  private readonly ruta = inject(ActivatedRoute);
  private readonly articuloService = inject(ArticuloService);
  private readonly carritoService = inject(CarritoService);
  private readonly usuario = inject(AuthService).usuarioActual;

  protected readonly articulo = signal<Articulo | null>(null);
  protected readonly cargando = signal(true);
  protected readonly noEncontrado = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly etiquetasCategoria = ETIQUETAS_CATEGORIA;

  // --- RF-04.1 / RF-04.2 ---
  protected readonly puedeComprar = computed(() => {
    const usuario = this.usuario();
    return usuario === null || usuario.rol === 'CLIENTE';
  });
  protected readonly cantidad = signal(1);
  protected readonly enCarrito = computed(() => {
    const articulo = this.articulo();
    // Lee la señal del carrito para que se actualice al agregar.
    return articulo ? this.carritoService.lineas().find((l) => l.articuloId === articulo.id)?.cantidad ?? 0 : 0;
  });
  /** Unidades que aún se pueden agregar: stock menos lo que ya está en el carrito. */
  protected readonly maximoAgregable = computed(() => {
    const articulo = this.articulo();
    if (!articulo) {
      return 0;
    }
    return Math.max(0, articulo.stock - this.enCarrito());
  });
  protected readonly agregando = signal(false);
  protected readonly aviso = signal<AvisoCarrito | null>(null);
  private temporizadorAviso?: ReturnType<typeof setTimeout>;

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

  cambiarCantidad(valor: number): void {
    const maximo = Math.max(1, this.maximoAgregable());
    this.cantidad.set(Math.min(Math.max(1, Math.floor(valor) || 1), maximo));
  }

  agregarAlCarrito(): void {
    const articulo = this.articulo();
    if (!articulo || this.agregando()) {
      return;
    }
    this.agregando.set(true);
    this.carritoService.agregar(articulo.id, this.cantidad()).subscribe({
      next: (resultado) => {
        this.agregando.set(false);
        this.cantidad.set(1);
        // El backend devuelve el stock actual: se refleja en la página.
        this.articulo.set({ ...articulo, stock: resultado.linea.stockDisponible, agotado: resultado.linea.stockDisponible === 0 });
        this.mostrarAviso(
          !resultado.aplicado
            ? { texto: resultado.mensaje ?? 'Este producto no se puede agregar.', tipo: 'error' }
            : resultado.mensaje
              ? { texto: resultado.mensaje, tipo: 'atencion' }
              : { texto: `«${articulo.nombre}» se agregó al carrito.`, tipo: 'exito' },
        );
      },
      error: (err: unknown) => {
        this.agregando.set(false);
        this.mostrarAviso({ texto: extraerMensajeError(err, 'No se pudo agregar al carrito.'), tipo: 'error' });
      },
    });
  }

  cerrarAviso(): void {
    this.aviso.set(null);
  }

  private mostrarAviso(aviso: AvisoCarrito): void {
    clearTimeout(this.temporizadorAviso);
    this.aviso.set(aviso);
    this.temporizadorAviso = setTimeout(() => this.aviso.set(null), 5000);
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
