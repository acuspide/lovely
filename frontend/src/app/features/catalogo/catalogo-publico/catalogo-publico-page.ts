import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';

import { AuthService } from '../../../core/auth/auth.service';
import { Articulo } from '../../../core/models/articulo.model';
import { CATEGORIAS, CategoriaArticulo, ETIQUETAS_CATEGORIA } from '../../../core/models/categoria-articulo';
import { ArticuloService } from '../articulo.service';

@Component({
  selector: 'app-catalogo-publico-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe],
  templateUrl: './catalogo-publico-page.html',
  styleUrl: './catalogo-publico-page.scss',
})
export class CatalogoPublicoPage {
  private readonly articuloService = inject(ArticuloService);
  protected readonly authService = inject(AuthService);

  protected readonly articulos = signal<Articulo[]>([]);
  protected readonly cargando = signal(true);
  protected readonly categoriaActiva = signal<CategoriaArticulo | null>(null);
  protected readonly etiquetasCategoria = ETIQUETAS_CATEGORIA;
  protected readonly categorias = CATEGORIAS;

  protected readonly buscador = new FormControl('', { nonNullable: true });

  constructor() {
    this.cargar();
    this.buscador.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => this.cargar());
  }

  filtrarPorCategoria(categoria: CategoriaArticulo | null): void {
    this.categoriaActiva.set(categoria);
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);
    this.articuloService
      .listar({
        categoria: this.categoriaActiva() ?? undefined,
        buscar: this.buscador.value || undefined,
      })
      .subscribe({
        next: (articulos) => {
          this.articulos.set(articulos);
          this.cargando.set(false);
        },
        error: () => this.cargando.set(false),
      });
  }
}
