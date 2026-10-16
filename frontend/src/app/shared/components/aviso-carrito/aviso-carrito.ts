import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Aviso que se muestra al agregar un producto al carrito. */
export interface AvisoCarrito {
  texto: string;
  /** 'exito': se agregó; 'atencion': se agregó con ajuste; 'error': no se pudo agregar. */
  tipo: 'exito' | 'atencion' | 'error';
}

/**
 * RF-04.1 / RF-04.2: confirmación flotante después de agregar al carrito, con
 * acceso directo a "Ver carrito". Es presentacional: el padre decide cuándo
 * mostrarla y la cierra con `cerrar`.
 */
@Component({
  selector: 'app-aviso-carrito',
  imports: [RouterLink],
  template: `
    @if (aviso(); as a) {
      <div class="aviso" [class.atencion]="a.tipo === 'atencion'" [class.error]="a.tipo === 'error'" role="status">
        <span>{{ a.texto }}</span>
        @if (a.tipo !== 'error') {
          <a routerLink="/carrito" class="ver-carrito">Ver carrito</a>
        }
        <button type="button" class="cerrar" aria-label="Cerrar aviso" (click)="cerrar.emit()">×</button>
      </div>
    }
  `,
  styles: `
    .aviso {
      position: fixed;
      left: 50%;
      bottom: 1.25rem;
      transform: translateX(-50%);
      z-index: 20;
      display: flex;
      align-items: center;
      gap: 0.9rem;
      max-width: calc(100vw - 2rem);
      padding: 0.8rem 1rem;
      border-radius: 12px;
      background: var(--color-success-bg);
      color: var(--color-success);
      box-shadow: var(--sombra-tarjeta);
      font-size: 0.9rem;
      font-weight: 600;

      &.atencion {
        background: #fff4e0;
        color: #8a5a00;
      }

      &.error {
        background: var(--color-danger-bg);
        color: var(--color-danger);
      }
    }

    .ver-carrito {
      color: inherit;
      white-space: nowrap;
    }

    .cerrar {
      border: none;
      background: none;
      color: inherit;
      font-size: 1.2rem;
      line-height: 1;
      cursor: pointer;
    }
  `,
})
export class AvisoCarritoComponent {
  readonly aviso = input<AvisoCarrito | null>(null);
  readonly cerrar = output<void>();
}
