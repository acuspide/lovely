import { Component, ElementRef, computed, effect, input, output, signal, viewChild } from '@angular/core';

import { MAX_MOTIVO_CANCELACION } from '../../../core/pedidos/pedido.model';

/**
 * RF-07.5: ventana para cancelar un pedido. Pide el motivo (obligatorio, hasta
 * 300 caracteres) y avisa que las unidades vuelven al inventario (RF-07.6).
 * La usan la clienta ("Mis pedidos") y la Administradora (gestión).
 *
 * Igual que {@link DialogoConfirmacion}, usa el `<dialog>` nativo y es
 * presentacional: el padre decide cuándo abrirla y qué hacer con el motivo.
 *
 * @example
 * <app-dialogo-cancelar-pedido [abierto]="porCancelar() !== null" [numeroPedido]="porCancelar()?.numero ?? ''"
 *   [enviando]="cancelando()" [error]="errorCancelar()"
 *   (confirmar)="cancelar($event)" (cerrar)="porCancelar.set(null)" />
 */
@Component({
  selector: 'app-dialogo-cancelar-pedido',
  template: `
    <dialog
      #dialogo
      class="dialogo"
      aria-labelledby="cancelar-titulo"
      (cancel)="alPresionarEscape($event)"
      (click)="alHacerClicFuera($event)"
    >
      <form class="contenido-dialogo" (submit)="enviar($event)">
        <h2 id="cancelar-titulo">Cancelar pedido {{ numeroPedido() }}</h2>
        <p>Las unidades del pedido vuelven al inventario. Esta acción no se puede deshacer.</p>

        <div class="campo">
          <label for="motivo-cancelacion">Motivo de la cancelación</label>
          <textarea
            id="motivo-cancelacion"
            rows="3"
            [maxLength]="maximo"
            [value]="motivo()"
            (input)="motivo.set($any($event.target).value)"
            aria-describedby="ayuda-motivo"
          ></textarea>
          <span id="ayuda-motivo" class="campo-ayuda">{{ motivo().length }} / {{ maximo }}</span>
        </div>

        @if (error(); as mensaje) {
          <div class="alerta alerta-error" role="alert">{{ mensaje }}</div>
        }

        <div class="acciones">
          <button type="button" class="btn btn-secundario" autofocus (click)="cerrar.emit()">Volver</button>
          <button type="submit" class="btn btn-peligro-solido" [disabled]="!motivoValido() || enviando()">
            {{ enviando() ? 'Cancelando…' : 'Cancelar pedido' }}
          </button>
        </div>
      </form>
    </dialog>
  `,
  styles: `
    .dialogo {
      border: none;
      border-radius: var(--radio-borde);
      box-shadow: var(--sombra-tarjeta);
      padding: 0;
      width: min(460px, calc(100vw - 2rem));
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
        margin: 0 0 1rem;
        line-height: 1.45;
        color: var(--color-text-muted);
      }

      textarea {
        width: 100%;
        box-sizing: border-box;
        padding: 0.65rem 0.8rem;
        border: 1px solid var(--color-border);
        border-radius: 8px;
        font-family: inherit;
        resize: vertical;
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

      &:disabled {
        opacity: 0.55;
        cursor: not-allowed;
      }
    }
  `,
})
export class DialogoCancelarPedido {
  readonly abierto = input(false);
  readonly numeroPedido = input('');
  readonly enviando = input(false);
  readonly error = input<string | null>(null);

  /** Emite el motivo ya sin espacios sobrantes. */
  readonly confirmar = output<string>();
  readonly cerrar = output<void>();

  protected readonly maximo = MAX_MOTIVO_CANCELACION;
  protected readonly motivo = signal('');
  protected readonly motivoValido = computed(() => {
    const limpio = this.motivo().trim();
    return limpio.length > 0 && limpio.length <= this.maximo;
  });

  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');

  constructor() {
    effect(() => {
      const elemento = this.dialogo().nativeElement;
      if (this.abierto() && !elemento.open) {
        this.motivo.set('');
        elemento.showModal();
      } else if (!this.abierto() && elemento.open) {
        elemento.close();
      }
    });
  }

  protected enviar(evento: Event): void {
    evento.preventDefault();
    if (this.motivoValido() && !this.enviando()) {
      this.confirmar.emit(this.motivo().trim());
    }
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
