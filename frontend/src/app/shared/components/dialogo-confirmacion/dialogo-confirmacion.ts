import { Component, ElementRef, effect, input, output, viewChild } from '@angular/core';

/**
 * Ventana de confirmación con el estilo de la app, en lugar del `confirm()`
 * del navegador (RF-02.8: pedir confirmación antes de eliminar un producto).
 *
 * Usa el elemento nativo `<dialog>`, que ya resuelve la accesibilidad básica:
 * bloquea el resto de la página, atrapa el foco dentro de la ventana y se
 * cierra con la tecla Esc. El foco inicial queda en "Cancelar", para que una
 * acción destructiva nunca se confirme por accidente con Enter.
 *
 * Es un componente "presentacional": no decide nada por sí mismo. El padre
 * controla cuándo se abre (`abierto`) y qué hacer con la respuesta
 * (`confirmar` / `cancelar`).
 *
 * @example
 * <app-dialogo-confirmacion
 *   [abierto]="!!articuloPorEliminar()"
 *   titulo="Eliminar producto"
 *   [mensaje]="'¿Eliminar ' + articuloPorEliminar()?.nombre + '?'"
 *   textoConfirmar="Sí, eliminar"
 *   [peligroso]="true"
 *   (confirmar)="confirmarEliminacion()"
 *   (cancelar)="articuloPorEliminar.set(null)" />
 */
@Component({
  selector: 'app-dialogo-confirmacion',
  template: `
    <dialog
      #dialogo
      class="dialogo"
      aria-labelledby="dialogo-titulo"
      aria-describedby="dialogo-mensaje"
      (cancel)="alPresionarEscape($event)"
      (click)="alHacerClicFuera($event)"
    >
      <!-- El contenido va en un div para distinguir un clic "dentro" de uno en el fondo. -->
      <div class="contenido-dialogo">
        <h2 id="dialogo-titulo">{{ titulo() }}</h2>
        <p id="dialogo-mensaje">{{ mensaje() }}</p>
        @if (detalle()) {
          <p class="detalle">{{ detalle() }}</p>
        }
        <div class="acciones">
          <button type="button" class="btn btn-secundario" autofocus (click)="cancelar.emit()">
            {{ textoCancelar() }}
          </button>
          <button
            type="button"
            class="btn"
            [class.btn-primary]="!peligroso()"
            [class.btn-peligro-solido]="peligroso()"
            (click)="confirmar.emit()"
          >
            {{ textoConfirmar() }}
          </button>
        </div>
      </div>
    </dialog>
  `,
  styles: `
    .dialogo {
      border: none;
      border-radius: var(--radio-borde);
      box-shadow: var(--sombra-tarjeta);
      padding: 0;
      width: min(440px, calc(100vw - 2rem));
      color: var(--color-text);

      &::backdrop {
        background: rgba(58, 35, 64, 0.45);
      }

      h2 {
        margin: 0 0 0.75rem;
        color: var(--color-primary-dark);
        font-size: 1.25rem;
      }

      p {
        margin: 0 0 0.75rem;
        line-height: 1.45;
      }

      .detalle {
        font-size: 0.9rem;
        color: var(--color-text-muted);
      }
    }

    .contenido-dialogo {
      padding: 1.75rem;
    }

    .acciones {
      display: flex;
      justify-content: flex-end;
      gap: 0.75rem;
      margin-top: 1.25rem;
    }

    .btn-peligro-solido {
      background: var(--color-danger);
      color: #fff;
      border: 1px solid var(--color-danger);

      &:hover {
        filter: brightness(0.92);
      }
    }
  `,
})
export class DialogoConfirmacion {
  /** Si es true la ventana se muestra; el padre la cierra poniéndolo en false. */
  readonly abierto = input(false);
  readonly titulo = input.required<string>();
  readonly mensaje = input.required<string>();
  /** Texto secundario opcional (p. ej. una consecuencia o una alternativa). */
  readonly detalle = input<string | null>(null);
  readonly textoConfirmar = input('Confirmar');
  readonly textoCancelar = input('Cancelar');
  /** Pinta el botón de confirmar en rojo, para acciones que no se pueden deshacer. */
  readonly peligroso = input(false);

  readonly confirmar = output<void>();
  readonly cancelar = output<void>();

  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');

  constructor() {
    // Sincroniza el estado `abierto` con el <dialog> nativo.
    effect(() => {
      const elemento = this.dialogo().nativeElement;
      if (this.abierto() && !elemento.open) {
        elemento.showModal();
      } else if (!this.abierto() && elemento.open) {
        elemento.close();
      }
    });
  }

  /** Esc: en vez de dejar que el navegador cierre la ventana, avisa al padre. */
  protected alPresionarEscape(evento: Event): void {
    evento.preventDefault();
    this.cancelar.emit();
  }

  /** Un clic en el fondo oscuro (fuera del recuadro) equivale a cancelar. */
  protected alHacerClicFuera(evento: MouseEvent): void {
    if (evento.target === this.dialogo().nativeElement) {
      this.cancelar.emit();
    }
  }
}
