import { Component, inject, input } from '@angular/core';

import { AuthService } from '../../../core/auth/auth.service';
import { BarraSuperior } from '../barra-superior/barra-superior';

/**
 * Pantalla de inicio provisional para los roles cuya funcionalidad todavía no
 * está construida (Asesora: IU-04; Encargada de Inventario: IU-05). Existe
 * para que RF-01.3 ya lleve a cada rol a su ruta; el contenido llegará con su
 * propia funcionalidad.
 */
@Component({
  selector: 'app-pantalla-en-construccion',
  imports: [BarraSuperior],
  template: `
    <app-barra-superior [seccion]="titulo()" />
    <main class="contenido">
      <section class="tarjeta">
        <h2>¡Hola, {{ usuario()?.nombre }}!</h2>
        <p>{{ descripcion() }}</p>
        <p class="nota">Esta pantalla ({{ codigo() }}) se completará con su propia funcionalidad.</p>
      </section>
    </main>
  `,
  styles: `
    @use '../../styles/layout';

    .contenido {
      @include layout.contenido-app;
    }

    .nota {
      color: var(--color-text-muted);
      font-size: 0.9rem;
    }
  `,
})
export class PantallaEnConstruccion {
  readonly titulo = input.required<string>();
  readonly codigo = input.required<string>();
  readonly descripcion = input.required<string>();

  protected readonly usuario = inject(AuthService).usuarioActual;
}
