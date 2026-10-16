import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';

import { Articulo } from '../../../../core/models/articulo.model';
import { CATEGORIAS, CategoriaArticulo, ETIQUETAS_CATEGORIA } from '../../../../core/models/categoria-articulo';
import { extraerMensajeError } from '../../../../core/http/mensaje-error';
import { BarraSuperior } from '../../../../shared/components/barra-superior/barra-superior';
import { DialogoConfirmacion } from '../../../../shared/components/dialogo-confirmacion/dialogo-confirmacion';
import { UrlImagenPipe } from '../../../../shared/pipes/url-imagen.pipe';
import { DetalleArticulo } from '../detalle-articulo/detalle-articulo';
import { SelectorImagen } from '../selector-imagen/selector-imagen';
import { ArticuloService } from '../../../catalogo/articulo.service';

interface ArticuloForm {
  nombre: FormControl<string>;
  descripcion: FormControl<string>;
  precio: FormControl<number>;
  categoria: FormControl<CategoriaArticulo>;
  marca: FormControl<string>;
  /** RF-02.4: URL devuelta por la subida, o null si el producto va sin imagen (CU-01 A4). */
  imagenUrl: FormControl<string | null>;
  /** RF-02.6: solo se usa al crear; en edición queda deshabilitado. */
  stockInicial: FormControl<number>;
}

@Component({
  selector: 'app-articulos-page',
  standalone: true,
  imports: [ReactiveFormsModule, BarraSuperior, DialogoConfirmacion, DetalleArticulo, SelectorImagen, UrlImagenPipe],
  templateUrl: './articulos-page.html',
  styleUrl: './articulos-page.scss',
})
export class ArticulosPage {
  private readonly articuloService = inject(ArticuloService);

  protected readonly articulos = signal<Articulo[]>([]);
  protected readonly cargando = signal(true);
  protected readonly enviando = signal(false);
  /** RF-02.4: mientras sube una imagen no se puede guardar el producto. */
  protected readonly subiendoImagen = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);
  /**
   * RF-02.9: producto que no se pudo eliminar por tener pedidos activos. Mientras
   * tenga valor, el aviso de error ofrece el botón "Desactivar en su lugar".
   */
  protected readonly sugerirDesactivar = signal<Articulo | null>(null);
  /** RF-02.8: producto que espera confirmación para eliminarse (null = ventana cerrada). */
  protected readonly articuloPorEliminar = signal<Articulo | null>(null);
  protected readonly editando = signal<Articulo | null>(null);

  // --- RF-02.5: consulta del catálogo ---
  /** Búsqueda por nombre (se envía al backend, que ignora mayúsculas y tildes). */
  protected readonly buscador = new FormControl('', { nonNullable: true });
  protected readonly categoriaFiltro = signal<CategoriaArticulo | null>(null);
  /** Filtro por estado (RF-02.7); se aplica en el navegador sobre lo ya cargado. */
  protected readonly estadoFiltro = signal<'TODOS' | 'ACTIVOS' | 'INACTIVOS'>('TODOS');
  protected readonly articulosVisibles = computed(() => {
    const estado = this.estadoFiltro();
    return this.articulos().filter(
      (a) => estado === 'TODOS' || (estado === 'ACTIVOS' ? a.activo : !a.activo),
    );
  });
  private readonly textoBuscado = signal('');
  protected readonly hayFiltros = computed(
    () => this.categoriaFiltro() !== null || this.estadoFiltro() !== 'TODOS' || this.textoBuscado() !== '',
  );
  /** Id del producto abierto en la ventana de detalle (null = cerrada). */
  protected readonly detalleId = signal<number | null>(null);
  protected readonly etiquetasCategoria = ETIQUETAS_CATEGORIA;
  protected readonly categorias = CATEGORIAS;

  protected readonly form = new FormGroup<ArticuloForm>({
    nombre: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    descripcion: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    precio: new FormControl(0, { nonNullable: true, validators: [Validators.required, Validators.min(0.01)] }),
    categoria: new FormControl<CategoriaArticulo>('FACIAL', { nonNullable: true, validators: [Validators.required] }),
    marca: new FormControl('', { nonNullable: true }),
    imagenUrl: new FormControl<string | null>(null),
    stockInicial: new FormControl(0, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)],
    }),
  });

  constructor() {
    this.cargarArticulos();
    this.buscador.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe((texto) => {
        this.textoBuscado.set(texto.trim());
        this.cargarArticulos();
      });
  }

  filtrarPorCategoria(valor: string): void {
    this.categoriaFiltro.set(valor ? (valor as CategoriaArticulo) : null);
    this.cargarArticulos();
  }

  filtrarPorEstado(valor: string): void {
    this.estadoFiltro.set(valor as 'TODOS' | 'ACTIVOS' | 'INACTIVOS');
  }

  limpiarFiltros(): void {
    this.categoriaFiltro.set(null);
    this.estadoFiltro.set('TODOS');
    this.buscador.setValue('', { emitEvent: false });
    this.textoBuscado.set('');
    this.cargarArticulos();
  }

  /** RF-02.5: abre la ventana con el detalle completo del producto. */
  verDetalle(articulo: Articulo): void {
    this.detalleId.set(articulo.id);
  }

  /** Desde el detalle se puede pasar directo a editar. */
  editarDesdeDetalle(articulo: Articulo): void {
    this.detalleId.set(null);
    this.editar(articulo);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  private cargarArticulos(): void {
    this.cargando.set(true);
    // RF-02.7: el panel ve también los productos desactivados, para poder reactivarlos.
    this.articuloService
      .listarParaGestion({
        categoria: this.categoriaFiltro() ?? undefined,
        buscar: this.textoBuscado() || undefined,
      })
      .subscribe({
        next: (articulos) => {
          this.articulos.set(articulos);
          this.cargando.set(false);
        },
        error: (err: unknown) => {
          this.cargando.set(false);
          this.error.set(extraerMensajeError(err, 'No se pudo cargar el catálogo.'));
        },
      });
  }

  editar(articulo: Articulo): void {
    this.editando.set(articulo);
    this.form.setValue({
      nombre: articulo.nombre,
      descripcion: articulo.descripcion,
      precio: articulo.precio,
      categoria: articulo.categoria,
      marca: articulo.marca ?? '',
      imagenUrl: articulo.imagenUrl ?? null,
      stockInicial: articulo.stock,
    });
    // RF-02.6: el stock de un producto existente no se edita aquí (lo maneja Inventario).
    this.form.controls.stockInicial.disable();
  }

  /** RF-02.4: el selector de imagen entrega la URL subida (o null si se quitó). */
  alCambiarImagen(url: string | null): void {
    this.form.controls.imagenUrl.setValue(url);
  }

  cancelarEdicion(): void {
    this.editando.set(null);
    this.form.reset({ nombre: '', descripcion: '', precio: 0, categoria: 'FACIAL', marca: '', imagenUrl: null, stockInicial: 0 });
    this.form.controls.stockInicial.enable();
  }

  guardar(): void {
    if (this.form.invalid || this.enviando() || this.subiendoImagen()) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.limpiarAvisos();

    const { stockInicial, ...valores } = this.form.getRawValue();
    const datos = { ...valores, marca: valores.marca.trim() || null };
    const enEdicion = this.editando();

    // RF-02.6: al editar no se envía el stock; al crear se envía como stock inicial.
    const peticion = enEdicion
      ? this.articuloService.actualizar(enEdicion.id, datos)
      : this.articuloService.crear({ ...datos, stockInicial });

    peticion.subscribe({
      next: (articulo) => {
        this.enviando.set(false);
        if (enEdicion) {
          this.reemplazarEnLista(articulo);
          this.exito.set(`Se actualizó "${articulo.nombre}".`);
        } else {
          // Se recarga para respetar el orden alfabético y los filtros activos.
          this.cargarArticulos();
          // CU-01 paso 7: mensaje de éxito con el ID del nuevo producto.
          this.exito.set(`Se creó "${articulo.nombre}" con el ID ${articulo.id}.`);
        }
        this.cancelarEdicion();
      },
      error: (err: unknown) => {
        this.enviando.set(false);
        this.error.set(extraerMensajeError(err, 'Ocurrió un error. Intenta de nuevo.'));
      },
    });
  }

  /** RF-02.8: antes de eliminar se pide confirmación en una ventana propia. */
  eliminar(articulo: Articulo): void {
    this.articuloPorEliminar.set(articulo);
  }

  cancelarEliminacion(): void {
    this.articuloPorEliminar.set(null);
  }

  confirmarEliminacion(): void {
    const articulo = this.articuloPorEliminar();
    if (!articulo) {
      return;
    }
    this.articuloPorEliminar.set(null);
    this.limpiarAvisos();
    this.articuloService.eliminar(articulo.id).subscribe({
      next: () => {
        this.articulos.update((actuales) => actuales.filter((a) => a.id !== articulo.id));
        this.exito.set(`Se eliminó "${articulo.nombre}".`);
      },
      error: (err: unknown) => {
        this.error.set(extraerMensajeError(err, 'Ocurrió un error. Intenta de nuevo.'));
        // RF-02.9: el backend indica que la alternativa es desactivarlo.
        if (err instanceof HttpErrorResponse && err.error?.accionSugerida === 'DESACTIVAR') {
          this.sugerirDesactivar.set(articulo);
        }
      },
    });
  }

  /** RF-02.7: oculta el producto del catálogo (o lo vuelve a mostrar) sin borrarlo. */
  cambiarEstado(articulo: Articulo): void {
    this.limpiarAvisos();
    const activar = !articulo.activo;

    this.articuloService.cambiarEstado(articulo.id, activar).subscribe({
      next: (actualizado) => {
        this.reemplazarEnLista(actualizado);
        this.exito.set(
          activar
            ? `"${actualizado.nombre}" volvió a estar visible en el catálogo.`
            : `"${actualizado.nombre}" quedó desactivado: ya no se muestra en el catálogo.`,
        );
      },
      error: (err: unknown) => this.error.set(extraerMensajeError(err, 'Ocurrió un error. Intenta de nuevo.')),
    });
  }

  private reemplazarEnLista(articulo: Articulo): void {
    this.articulos.update((actuales) => actuales.map((a) => (a.id === articulo.id ? articulo : a)));
  }

  private limpiarAvisos(): void {
    this.error.set(null);
    this.exito.set(null);
    this.sugerirDesactivar.set(null);
  }
}
