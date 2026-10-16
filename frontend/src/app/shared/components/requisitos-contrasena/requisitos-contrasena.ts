import { Component, computed, input } from '@angular/core';

import {
  CONTRASENA_LONGITUD_MAXIMA,
  CONTRASENA_LONGITUD_MINIMA,
  evaluarContrasena,
} from '../../validators/cuenta.validators';

/**
 * Lista de requisitos de la contraseña (RF-01.1) que se marcan a medida que
 * se cumplen. Se reutiliza en registro, recuperación, perfil y alta de personal.
 */
@Component({
  selector: 'app-requisitos-contrasena',
  template: `
    <ul class="requisitos" [id]="idLista()" aria-live="polite">
      <li [class.cumplido]="requisitos().longitud">De {{ minimo }} a {{ maximo }} caracteres de largo</li>
      <li [class.cumplido]="requisitos().letra">Una letra (a-z)</li>
      <li [class.cumplido]="requisitos().numero">Un número (0-9)</li>
      <li [class.cumplido]="requisitos().especial">Un carácter especial (! @ # $ * . -)</li>
    </ul>
  `,
  styles: `
    .requisitos {
      list-style: none;
      margin: 0.25rem 0 0;
      padding: 0;
      display: flex;
      flex-wrap: wrap;
      gap: 0.25rem 0.9rem;
      font-size: 0.8rem;
      color: var(--color-text-muted);

      li::before {
        content: '○ ';
      }

      li.cumplido {
        color: var(--color-success);

        &::before {
          content: '✓ ';
        }
      }
    }
  `,
})
export class RequisitosContrasena {
  readonly valor = input<string>('');
  readonly idLista = input<string>('requisitos-contrasena');

  protected readonly minimo = CONTRASENA_LONGITUD_MINIMA;
  protected readonly maximo = CONTRASENA_LONGITUD_MAXIMA;

  protected readonly requisitos = computed(() => evaluarContrasena(this.valor() ?? ''));
}
