import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';
import { RevisionCarrito } from '../../../core/carrito/carrito.model';
import { CarritoService } from '../../../core/carrito/carrito.service';
import { DatosEntrega, OPCIONES_ENTREGA, TipoEntrega } from '../../../core/carrito/entrega.model';
import { extraerMensajeError } from '../../../core/http/mensaje-error';
import { BarraSuperior } from '../../../shared/components/barra-superior/barra-superior';
import { telefonoValidator } from '../../../shared/validators/cuenta.validators';

interface EntregaForm {
  tipo: FormControl<TipoEntrega>;
  nombreContacto: FormControl<string>;
  telefono: FormControl<string>;
  direccion: FormControl<string>;
  ciudad: FormControl<string>;
  indicaciones: FormControl<string>;
}

/**
 * Confirmar pedido (CU-02 pasos 7 a 10). Solo para clientas con sesión: si no
 * hay sesión, el guard la lleva a iniciar sesión o registrarse y la devuelve
 * aquí con su carrito intacto (RF-04.5).
 *
 * - RF-04.6: tipo de entrega y datos de contacto y entrega.
 * - RF-04.7: al confirmar se revisa otra vez todo el carrito. Si algo se agotó
 *   o ya no alcanza, se ajusta, se avisa y se pide confirmar de nuevo (CU-02 A3).
 *
 * El registro del pedido y el descuento de stock son F-05 y F-06.
 */
@Component({
  selector: 'app-confirmar-pedido-page',
  imports: [DecimalPipe, ReactiveFormsModule, RouterLink, BarraSuperior],
  templateUrl: './confirmar-pedido-page.html',
  styleUrl: './confirmar-pedido-page.scss',
})
export class ConfirmarPedidoPage {
  private readonly carritoService = inject(CarritoService);
  private readonly router = inject(Router);
  private readonly usuario = inject(AuthService).usuarioActual;

  protected readonly opciones = OPCIONES_ENTREGA;
  protected readonly revision = signal<RevisionCarrito | null>(null);
  protected readonly cargando = signal(true);
  protected readonly confirmando = signal(false);
  protected readonly error = signal<string | null>(null);
  /** RF-04.7: avisos de lo que cambió en la revisión final. */
  protected readonly avisos = signal<string[]>([]);
  /** Mensaje cuando el carrito pasó la revisión final y quedó listo para registrarse. */
  protected readonly listo = signal<string | null>(null);

  protected readonly form = new FormGroup<EntregaForm>({
    tipo: new FormControl<TipoEntrega>('RETIRO_EN_TIENDA', { nonNullable: true }),
    nombreContacto: new FormControl(this.usuario()?.nombre ?? '', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(120)],
    }),
    telefono: new FormControl(this.usuario()?.telefono ?? '', {
      nonNullable: true,
      validators: [Validators.required, telefonoValidator],
    }),
    direccion: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(200)] }),
    ciudad: new FormControl('Armenia', { nonNullable: true, validators: [Validators.maxLength(80)] }),
    indicaciones: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(300)] }),
  });

  private readonly tipoElegido = toSignal(this.form.controls.tipo.valueChanges, {
    initialValue: this.form.controls.tipo.value,
  });
  /** RF-04.6: domicilio y envío piden dirección y ciudad; el retiro en tienda no. */
  protected readonly requiereDireccion = computed(
    () => this.opciones.find((o) => o.valor === this.tipoElegido())?.requiereDireccion ?? false,
  );
  protected readonly lineas = computed(() => (this.revision()?.lineas ?? []).filter((l) => l.cantidad > 0));

  constructor() {
    this.form.controls.tipo.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.ajustarValidadores());
    this.ajustarValidadores();
    this.revisar();
  }

  /** CU-02 paso 10 / RF-04.7: revisión final antes de registrar el pedido. */
  confirmar(): void {
    if (this.form.invalid || this.confirmando()) {
      this.form.markAllAsTouched();
      return;
    }
    this.confirmando.set(true);
    this.error.set(null);
    this.listo.set(null);

    this.carritoService.revisar().subscribe({
      next: (revision) => {
        this.revision.set(revision);
        this.confirmando.set(false);
        if (!revision.tieneProductosComprables) {
          this.avisos.set(this.mensajesDe(revision));
          this.error.set('Ningún producto de tu carrito está disponible. Vuelve al catálogo para elegir otros.');
          return;
        }
        if (revision.requiereAtencion) {
          // CU-02 A3: se avisa y se pide confirmar de nuevo con el carrito ya ajustado.
          this.avisos.set(this.mensajesDe(revision));
          this.error.set('Algunos productos cambiaron. Revisa el resumen y vuelve a confirmar si estás de acuerdo.');
          return;
        }
        this.avisos.set([]);
        this.registrarPedido(this.datosEntrega(), revision);
      },
      error: (err: unknown) => {
        this.confirmando.set(false);
        this.error.set(extraerMensajeError(err, 'No se pudo revisar tu carrito. Intenta de nuevo.'));
      },
    });
  }

  /**
   * Punto de enlace con F-05 (registro del pedido) y F-06 (descuento de stock).
   * Por ahora confirma que el carrito pasó la revisión final.
   */
  private registrarPedido(_datos: DatosEntrega, revision: RevisionCarrito): void {
    this.listo.set(
      `Todo listo: tus ${revision.unidades} productos están disponibles por $ ${revision.total.toLocaleString('es-CO')}. ` +
        'El registro del pedido se habilita con F-05.',
    );
  }

  private datosEntrega(): DatosEntrega {
    const valores = this.form.getRawValue();
    const conDireccion = this.requiereDireccion();
    return {
      tipo: valores.tipo,
      nombreContacto: valores.nombreContacto.trim(),
      telefono: valores.telefono.trim(),
      direccion: conDireccion ? valores.direccion.trim() : null,
      ciudad: conDireccion ? valores.ciudad.trim() : null,
      indicaciones: valores.indicaciones.trim() || null,
    };
  }

  private ajustarValidadores(): void {
    const { direccion, ciudad } = this.form.controls;
    const requerido = this.requiereDireccion();
    for (const control of [direccion, ciudad]) {
      if (requerido) {
        control.addValidators(Validators.required);
      } else {
        control.removeValidators(Validators.required);
      }
      control.updateValueAndValidity({ emitEvent: false });
    }
  }

  private revisar(): void {
    this.carritoService.revisar().subscribe({
      next: (revision) => {
        this.cargando.set(false);
        if (revision.lineas.length === 0) {
          this.router.navigateByUrl('/carrito', { replaceUrl: true });
          return;
        }
        this.revision.set(revision);
        this.avisos.set(this.mensajesDe(revision));
      },
      error: (err: unknown) => {
        this.cargando.set(false);
        this.error.set(extraerMensajeError(err, 'No se pudo revisar tu carrito. Intenta de nuevo.'));
      },
    });
  }

  private mensajesDe(revision: RevisionCarrito): string[] {
    return revision.lineas.map((l) => l.mensaje).filter((m): m is string => m !== null);
  }
}
