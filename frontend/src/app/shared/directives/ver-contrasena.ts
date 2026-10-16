import { Directive, ElementRef, OnDestroy, OnInit, Renderer2, inject } from '@angular/core';

const ICONO_OJO =
  '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" ' +
  'stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' +
  '<path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12z"/><circle cx="12" cy="12" r="3"/></svg>';

const ICONO_OJO_TACHADO =
  '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" ' +
  'stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' +
  '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 19c-7 0-11-7-11-7a18.45 18.45 0 0 1 5.06-5.94"/>' +
  '<path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 7 11 7a18.5 18.5 0 0 1-2.16 3.19"/>' +
  '<path d="M14.12 14.12a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>';

/**
 * Agrega a cualquier campo de contraseña un botón con un ojo para mostrar u
 * ocultar lo que se escribe. Se aplica sola a todo <input type="password">
 * del componente que la importe.
 */
@Directive({ selector: 'input[type="password"]' })
export class VerContrasena implements OnInit, OnDestroy {
  private readonly input = inject<ElementRef<HTMLInputElement>>(ElementRef).nativeElement;
  private readonly renderer = inject(Renderer2);
  private quitarEscucha?: () => void;

  ngOnInit(): void {
    const padre = this.input.parentNode;
    if (!padre) {
      return;
    }

    const contenedor = this.renderer.createElement('div') as HTMLElement;
    this.renderer.addClass(contenedor, 'campo-contrasena');
    this.renderer.insertBefore(padre, contenedor, this.input);
    this.renderer.appendChild(contenedor, this.input);

    const boton = this.renderer.createElement('button') as HTMLButtonElement;
    this.renderer.setAttribute(boton, 'type', 'button');
    this.renderer.addClass(boton, 'btn-ver-contrasena');
    this.actualizarBoton(boton, false);
    this.renderer.appendChild(contenedor, boton);

    this.quitarEscucha = this.renderer.listen(boton, 'click', () => {
      const mostrar = this.input.type === 'password';
      this.input.type = mostrar ? 'text' : 'password';
      this.actualizarBoton(boton, mostrar);
      this.input.focus();
    });
  }

  ngOnDestroy(): void {
    this.quitarEscucha?.();
  }

  private actualizarBoton(boton: HTMLButtonElement, visible: boolean): void {
    const texto = visible ? 'Ocultar contraseña' : 'Mostrar contraseña';
    boton.innerHTML = visible ? ICONO_OJO_TACHADO : ICONO_OJO;
    this.renderer.setAttribute(boton, 'aria-label', texto);
    this.renderer.setAttribute(boton, 'title', texto);
    this.renderer.setAttribute(boton, 'aria-pressed', String(visible));
  }
}
