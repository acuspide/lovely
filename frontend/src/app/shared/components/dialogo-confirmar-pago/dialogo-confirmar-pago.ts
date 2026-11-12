import { DecimalPipe } from '@angular/common';
import { Component, ElementRef, computed, effect, input, output, signal, viewChild } from '@angular/core';

import { DatosConfirmacionPago, MedioPago, OPCIONES_MEDIO_PAGO } from '../../../core/pedidos/pedido.model';

/** Fecha de hoy en formato yyyy-mm-dd, en la hora local del navegador. */
function hoyIso(): string {
  const ahora = new Date();
  const local = new Date(ahora.getTime() - ahora.getTimezoneOffset() * 60_000);
  return local.toISOString().slice(0, 10);
}

/**
 * F-09 / RN11: ventana con la que la Asesora de ventas o la Administradora
 * confirman que el dinero de un pedido llegó a Lovely Girl. Solo después de
 * esto el pedido pasa a «Confirmado» y se emite su factura.
 *
 * - RF-09.2: medio, referencia (obligatoria en transferencias), monto y fecha.
 * - RF-09.3: el monto viene con el total del pedido; si se cambia, el backend
 *   lo rechaza porque debe coincidir.
 * - RF-09.4: la fecha no puede ser futura.
 *
 * Es presentacional: el padre decide cuándo abrirla y qué hacer con los datos.
 */
@Component({
  selector: 'app-dialogo-confirmar-pago',
  imports: [DecimalPipe],
  template: `
    <dialog
      #dialogo
      class="dialogo"
      aria-labelledby="pago-titulo"
      (cancel)="alPresionarEscape($event)"
      (click)="alHacerClicFuera($event)"
    >
      <form class="contenido-dialogo" (submit)="enviar($event)">
        <h2 id="pago-titulo">Confirmar pago del pedido {{ numeroPedido() }}</h2>
        <p>
          Registra el pago solo después de verificar que el dinero llegó a la cuenta de Lovely Girl o de
          recibir el efectivo. Al confirmarlo, el pedido pasa a «Confirmado» y se emite la factura.
        </p>

        <div class="campo">
          <label for="pago-medio">Medio de pago</label>
          <select id="pago-medio" [value]="medio()" (change)="medio.set($any($event.target).value)">
            @for (opcion of opciones; track opcion.valor) {
              <option [value]="opcion.valor">{{ opcion.etiqueta }}</option>
            }
          </select>
        </div>

        <div class="campo">
          <label for="pago-referencia">
            Referencia {{ medio() === 'TRANSFERENCIA' ? '(número de la transacción)' : '(opcional)' }}
          </label>
          <input
            id="pago-referencia"
            type="text"
            maxlength="60"
            [value]="referencia()"
            (input)="referencia.set($any($event.target).value)"
          />
        </div>

        <div class="fila">
          <div class="campo">
            <label for="pago-monto">Monto recibido</label>
            <input
              id="pago-monto"
              type="number"
              min="1"
              step="1"
              [value]="monto()"
              (input)="monto.set(+$any($event.target).value)"
              aria-describedby="ayuda-monto"
            />
            <span id="ayuda-monto" class="campo-ayuda">Total del pedido: $ {{ total() | number: '1.0-0' }}</span>
          </div>
          <div class="campo">
            <label for="pago-fecha">Fecha de recepción</label>
            <input
              id="pago-fecha"
              type="date"
              [max]="hoy"
              [value]="fecha()"
              (input)="fecha.set($any($event.target).value)"
            />
          </div>
        </div>

        @if (error(); as mensaje) {
          <div class="alerta alerta-error" role="alert">{{ mensaje }}</div>
        }

        <div class="acciones">
          <button type="button" class="btn btn-secundario" (click)="cerrar.emit()">Volver</button>
          <button type="submit" class="btn btn-primary" [disabled]="!valido() || enviando()">
            {{ enviando() ? 'Confirmando…' : 'Confirmar pago' }}
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
      width: min(500px, calc(100vw - 2rem));
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

      input,
      select {
        width: 100%;
        box-sizing: border-box;
        padding: 0.6rem 0.75rem;
        border: 1px solid var(--color-border);
        border-radius: 8px;
        font-family: inherit;
        font-size: 1rem;
      }
    }

    .contenido-dialogo {
      padding: 1.75rem;
    }

    .campo {
      margin-bottom: 0.9rem;
    }

    .fila {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.75rem;
    }

    @media (max-width: 480px) {
      .fila {
        grid-template-columns: 1fr;
      }
    }

    .acciones {
      display: flex;
      justify-content: flex-end;
      gap: 0.75rem;
      margin-top: 1.25rem;
    }
  `,
})
export class DialogoConfirmarPago {
  readonly abierto = input(false);
  readonly numeroPedido = input('');
  readonly total = input(0);
  readonly enviando = input(false);
  readonly error = input<string | null>(null);

  readonly confirmar = output<DatosConfirmacionPago>();
  readonly cerrar = output<void>();

  protected readonly opciones = OPCIONES_MEDIO_PAGO;
  protected readonly hoy = hoyIso();
  protected readonly medio = signal<MedioPago>('TRANSFERENCIA');
  protected readonly referencia = signal('');
  protected readonly monto = signal(0);
  protected readonly fecha = signal(this.hoy);

  protected readonly valido = computed(() => {
    const referencia = this.referencia().trim();
    const referenciaValida =
      this.medio() === 'TRANSFERENCIA'
        ? referencia.length >= 4 && referencia.length <= 60
        : referencia.length === 0 || (referencia.length >= 4 && referencia.length <= 60);
    return referenciaValida && this.monto() > 0 && !!this.fecha() && this.fecha() <= this.hoy;
  });

  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');

  constructor() {
    effect(() => {
      const elemento = this.dialogo().nativeElement;
      if (this.abierto() && !elemento.open) {
        this.medio.set('TRANSFERENCIA');
        this.referencia.set('');
        this.monto.set(this.total());
        this.fecha.set(this.hoy);
        elemento.showModal();
      } else if (!this.abierto() && elemento.open) {
        elemento.close();
      }
    });
  }

  protected enviar(evento: Event): void {
    evento.preventDefault();
    if (!this.valido() || this.enviando()) {
      return;
    }
    const referencia = this.referencia().trim();
    this.confirmar.emit({
      medio: this.medio(),
      referencia: referencia.length ? referencia : null,
      monto: this.monto(),
      fechaRecepcion: this.fecha(),
    });
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
