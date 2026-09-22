import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Articulo } from '../../../../core/models/articulo.model';
import { CATEGORIAS, CategoriaArticulo, ETIQUETAS_CATEGORIA } from '../../../../core/models/categoria-articulo';
import { ArticuloService } from '../../../catalogo/articulo.service';

interface ArticuloForm {
  nombre: FormControl<string>;
  descripcion: FormControl<string>;
  precio: FormControl<number>;
  categoria: FormControl<CategoriaArticulo>;
  marca: FormControl<string>;
  imagenUrl: FormControl<string>;
  stock: FormControl<number>;
}

@Component({
  selector: 'app-articulos-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './articulos-page.html',
  styleUrl: './articulos-page.scss',
})
export class ArticulosPage {
  private readonly articuloService = inject(ArticuloService);

  protected readonly articulos = signal<Articulo[]>([]);
  protected readonly cargando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly editando = signal<Articulo | null>(null);
  protected readonly etiquetasCategoria = ETIQUETAS_CATEGORIA;
  protected readonly categorias = CATEGORIAS;

  protected readonly form = new FormGroup<ArticuloForm>({
    nombre: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    descripcion: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    precio: new FormControl(0, { nonNullable: true, validators: [Validators.required, Validators.min(0.01)] }),
    categoria: new FormControl<CategoriaArticulo>('FACIAL', { nonNullable: true, validators: [Validators.required] }),
    marca: new FormControl('', { nonNullable: true }),
    imagenUrl: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    stock: new FormControl(0, { nonNullable: true, validators: [Validators.required, Validators.min(0)] }),
  });

  constructor() {
    this.cargarArticulos();
  }

  private cargarArticulos(): void {
    this.cargando.set(true);
    this.articuloService.listar().subscribe({
      next: (articulos) => {
        this.articulos.set(articulos);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false),
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
      imagenUrl: articulo.imagenUrl,
      stock: articulo.stock,
    });
  }

  cancelarEdicion(): void {
    this.editando.set(null);
    this.form.reset({ nombre: '', descripcion: '', precio: 0, categoria: 'FACIAL', marca: '', imagenUrl: '', stock: 0 });
  }

  guardar(): void {
    if (this.form.invalid || this.enviando()) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.error.set(null);

    const valores = this.form.getRawValue();
    const payload = { ...valores, marca: valores.marca.trim() || null };
    const enEdicion = this.editando();

    const peticion = enEdicion
      ? this.articuloService.actualizar(enEdicion.id, payload)
      : this.articuloService.crear(payload);

    peticion.subscribe({
      next: (articulo) => {
        this.enviando.set(false);
        if (enEdicion) {
          this.articulos.update((actuales) => actuales.map((a) => (a.id === articulo.id ? articulo : a)));
        } else {
          this.articulos.update((actuales) => [...actuales, articulo]);
        }
        this.cancelarEdicion();
      },
      error: (err: unknown) => {
        this.enviando.set(false);
        this.error.set(this.extraerMensaje(err));
      },
    });
  }

  eliminar(articulo: Articulo): void {
    if (!confirm(`¿Eliminar "${articulo.nombre}"? Esta acción no se puede deshacer.`)) {
      return;
    }

    this.articuloService.eliminar(articulo.id).subscribe({
      next: () => this.articulos.update((actuales) => actuales.filter((a) => a.id !== articulo.id)),
      error: (err: unknown) => this.error.set(this.extraerMensaje(err)),
    });
  }

  private extraerMensaje(error: unknown): string {
    if (error instanceof HttpErrorResponse && typeof error.error?.detail === 'string') {
      return error.error.detail;
    }
    return 'Ocurrió un error. Intenta de nuevo.';
  }
}
