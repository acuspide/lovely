import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';

import { AuthService } from '../../../core/auth/auth.service';
import { CarritoService } from '../../../core/carrito/carrito.service';
import { extraerMensajeError } from '../../../core/http/mensaje-error';
import { Articulo } from '../../../core/models/articulo.model';
import { CATEGORIAS, CategoriaArticulo, ETIQUETAS_CATEGORIA } from '../../../core/models/categoria-articulo';
import { AvisoCarrito, AvisoCarritoComponent } from '../../../shared/components/aviso-carrito/aviso-carrito';
import { BarraSuperior } from '../../../shared/components/barra-superior/barra-superior';
import { UrlImagenPipe } from '../../../shared/pipes/url-imagen.pipe';
import { ArticuloService, OrdenCatalogo } from '../articulo.service';

/** RF-03.6: opciones del selector "Ordenar por", en el orden en que se muestran. */
const OPCIONES_ORDEN: { valor: OrdenCatalogo; etiqueta: string }[] = [
  { valor: 'NOMBRE', etiqueta: 'Nombre (A-Z)' },
  { valor: 'PRECIO_MENOR', etiqueta: 'Precio: menor a mayor' },
  { valor: 'PRECIO_MAYOR', etiqueta: 'Precio: mayor a menor' },
];

/**
 * IU-01: catálogo público de Lovely Girl (RF-03), visible con o sin sesión.
 *
 * - RF-03.1: solo productos activos (lo filtra el backend).
 * - RF-03.2 / RF-03.3: filtro por categoría y búsqueda por nombre.
 * - RF-03.4: los agotados se ven atenuados, con su aviso, y van al final.
 * - RF-03.5: cada tarjeta lleva al detalle del producto.
 * - RF-03.6 / RF-03.7: orden, contador de resultados y "Limpiar filtros".
 * - RF-04.1: botón "Agregar al carrito" (solo visitantes y clientas).
 */
@Component({
  selector: 'app-catalogo-publico-page',
  standalone: true,
  imports: [ReactiveFormsModule, DecimalPipe, RouterLink, BarraSuperior, UrlImagenPipe, AvisoCarritoComponent],
  templateUrl: './catalogo-publico-page.html',
  styleUrl: './catalogo-publico-page.scss',
})
export class CatalogoPublicoPage {
  private readonly articuloService = inject(ArticuloService);
  private readonly carritoService = inject(CarritoService);
  private readonly usuario = inject(AuthService).usuarioActual;

  protected readonly articulos = signal<Articulo[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly categoriaActiva = signal<CategoriaArticulo | null>(null);
  protected readonly orden = signal<OrdenCatalogo>('NOMBRE');
  protected readonly etiquetasCategoria = ETIQUETAS_CATEGORIA;
  protected readonly categorias = CATEGORIAS;
  protected readonly opcionesOrden = OPCIONES_ORDEN;

  protected readonly buscador = new FormControl('', { nonNullable: true });
  private readonly textoBuscado = signal('');

  /** RF-03.7: hay algo que limpiar (el orden no cuenta como filtro). */
  protected readonly hayFiltros = computed(() => this.categoriaActiva() !== null || this.textoBuscado() !== '');
  /** RF-04.1: el personal no compra desde la tienda; visitantes y clientas sí. */
  protected readonly puedeComprar = computed(() => {
    const usuario = this.usuario();
    return usuario === null || usuario.rol === 'CLIENTE';
  });
  /** Producto que se está agregando (para deshabilitar su botón mientras responde el backend). */
  protected readonly agregando = signal<number | null>(null);
  protected readonly aviso = signal<AvisoCarrito | null>(null);
  private temporizadorAviso?: ReturnType<typeof setTimeout>;

  /** RF-03.7: cuántos de los productos mostrados se pueden comprar ya. */
  protected readonly disponibles = computed(() => this.articulos().filter((a) => !a.agotado).length);

  constructor() {
    this.cargar();
    this.buscador.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe((texto) => {
        this.textoBuscado.set(texto.trim());
        this.cargar();
      });
  }

  filtrarPorCategoria(categoria: CategoriaArticulo | null): void {
    this.categoriaActiva.set(categoria);
    this.cargar();
  }

  ordenarPor(valor: string): void {
    this.orden.set(valor as OrdenCatalogo);
    this.cargar();
  }

  /** RF-03.7: vuelve al catálogo completo (conserva el orden elegido). */
  limpiarFiltros(): void {
    this.categoriaActiva.set(null);
    this.buscador.setValue('', { emitEvent: false });
    this.textoBuscado.set('');
    this.cargar();
  }

  /** RF-04.1 / RF-04.2: agrega una unidad, verificando stock en tiempo real con el backend. */
  agregarAlCarrito(articulo: Articulo): void {
    if (this.agregando() !== null) {
      return;
    }
    this.agregando.set(articulo.id);
    this.carritoService.agregar(articulo.id).subscribe({
      next: (resultado) => {
        this.agregando.set(null);
        this.mostrarAviso(
          !resultado.aplicado
            ? { texto: resultado.mensaje ?? 'Este producto no se puede agregar.', tipo: 'error' }
            : resultado.mensaje
              ? { texto: resultado.mensaje, tipo: 'atencion' }
              : { texto: `«${articulo.nombre}» se agregó al carrito.`, tipo: 'exito' },
        );
        if (!resultado.aplicado) {
          this.cargar(); // el stock cambió: se actualiza el catálogo para mostrarlo agotado
        }
      },
      error: (err: unknown) => {
        this.agregando.set(null);
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

  private cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.articuloService
      .listar({
        categoria: this.categoriaActiva() ?? undefined,
        buscar: this.textoBuscado() || undefined,
        orden: this.orden(),
      })
      .subscribe({
        next: (articulos) => {
          this.articulos.set(articulos);
          this.cargando.set(false);
        },
        error: (err: unknown) => {
          this.cargando.set(false);
          this.error.set(extraerMensajeError(err, 'No se pudo cargar el catálogo. Intenta de nuevo.'));
        },
      });
  }
}
