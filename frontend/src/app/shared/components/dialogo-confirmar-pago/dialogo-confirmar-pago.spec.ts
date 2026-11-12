import { TestBed } from '@angular/core/testing';

import { DatosConfirmacionPago } from '../../../core/pedidos/pedido.model';
import { DialogoConfirmarPago } from './dialogo-confirmar-pago';

/** F-09 (RF-09.2): la ventana exige la referencia en transferencias y envía los datos del pago. */
describe('DialogoConfirmarPago', () => {
  beforeAll(() => {
    // jsdom no implementa <dialog>.showModal()/close().
    HTMLDialogElement.prototype.showModal ??= function (this: HTMLDialogElement) {
      this.setAttribute('open', '');
    };
    HTMLDialogElement.prototype.close ??= function (this: HTMLDialogElement) {
      this.removeAttribute('open');
    };
  });

  function abrir(total: number) {
    const fixture = TestBed.createComponent(DialogoConfirmarPago);
    fixture.componentRef.setInput('total', total);
    fixture.componentRef.setInput('numeroPedido', 'LG-000007');
    fixture.componentRef.setInput('abierto', true);
    fixture.detectChanges();
    const raiz: HTMLElement = fixture.nativeElement;
    const enviados: DatosConfirmacionPago[] = [];
    fixture.componentInstance.confirmar.subscribe((datos) => enviados.push(datos));
    return { fixture, raiz, enviados };
  }

  function escribir(input: HTMLInputElement, valor: string) {
    input.value = valor;
    input.dispatchEvent(new Event('input'));
  }

  it('no deja confirmar una transferencia sin referencia', () => {
    const { fixture, raiz } = abrir(30000);
    const boton = raiz.querySelector<HTMLButtonElement>('button[type=submit]')!;

    expect(boton.disabled).toBe(true);

    escribir(raiz.querySelector<HTMLInputElement>('#pago-referencia')!, 'TRX-1234');
    fixture.detectChanges();
    expect(boton.disabled).toBe(false);
  });

  it('envía medio, referencia, monto (el total) y fecha', () => {
    const { fixture, raiz, enviados } = abrir(30000);
    escribir(raiz.querySelector<HTMLInputElement>('#pago-referencia')!, '  TRX-1234  ');
    fixture.detectChanges();

    raiz.querySelector<HTMLFormElement>('form')!.dispatchEvent(new Event('submit'));

    expect(enviados.length).toBe(1);
    expect(enviados[0].medio).toBe('TRANSFERENCIA');
    expect(enviados[0].referencia).toBe('TRX-1234');
    expect(enviados[0].monto).toBe(30000);
    expect(enviados[0].fechaRecepcion).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });
});
