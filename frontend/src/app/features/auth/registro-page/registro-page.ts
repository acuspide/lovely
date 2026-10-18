import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';
import { PARAMETRO_RETORNO, rutaRetornoSegura } from '../../../core/auth/ruta-retorno';
import { extraerMensajeError } from '../../../core/http/mensaje-error';
import { RequisitosContrasena } from '../../../shared/components/requisitos-contrasena/requisitos-contrasena';
import {
  camposCoincidenValidator,
  contrasenaSeguraValidator,
  telefonoValidator,
} from '../../../shared/validators/cuenta.validators';
import { VerContrasena } from '../../../shared/directives/ver-contrasena';

interface RegistroForm {
  nombre: FormControl<string>;
  email: FormControl<string>;
  telefono: FormControl<string>;
  contrasena: FormControl<string>;
  confirmarContrasena: FormControl<string>;
  aceptaHabeasData: FormControl<boolean>;
}

/** RF-01.1: una clienta crea su propia cuenta. */
@Component({
  selector: 'app-registro-page',
  imports: [ReactiveFormsModule, RouterLink, RequisitosContrasena, VerContrasena],
  templateUrl: './registro-page.html',
  styleUrl: './registro-page.scss',
})
export class RegistroPage {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  /** RF-04.5: ruta a la que volver tras registrarse (solo rutas internas). */
  protected readonly rutaRetorno = rutaRetornoSegura(
    inject(ActivatedRoute).snapshot.queryParamMap.get(PARAMETRO_RETORNO),
  );

  protected readonly enviando = signal(false);
  protected readonly errorGeneral = signal<string | null>(null);

  protected readonly form = new FormGroup<RegistroForm>(
    {
      nombre: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(150)] }),
      email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
      telefono: new FormControl('', { nonNullable: true, validators: [Validators.required, telefonoValidator] }),
      contrasena: new FormControl('', { nonNullable: true, validators: [Validators.required, contrasenaSeguraValidator] }),
      confirmarContrasena: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      aceptaHabeasData: new FormControl(false, { nonNullable: true, validators: [Validators.requiredTrue] }),
    },
    { validators: camposCoincidenValidator('contrasena', 'confirmarContrasena') },
  );

  protected readonly contrasenaEscrita = toSignal(this.form.controls.contrasena.valueChanges, { initialValue: '' });

  enviar(): void {
    if (this.form.invalid || this.enviando()) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.errorGeneral.set(null);

    const datos = this.form.getRawValue();

    this.authService.registro(datos).subscribe({
      next: () => this.iniciarSesionTrasRegistro(datos.email, datos.contrasena),
      error: (error: unknown) => {
        this.enviando.set(false);
        this.errorGeneral.set(extraerMensajeError(error, 'No se pudo completar el registro. Intenta de nuevo.'));
      },
    });
  }

  /**
   * Así la nueva clienta no tiene que volver a escribir sus datos; entra directo al catálogo (RF-01.3)
   * o, si se registró para comprar, vuelve a confirmar su pedido (RF-04.5).
   */
  private iniciarSesionTrasRegistro(email: string, contrasena: string): void {
    this.authService.login(email, contrasena).subscribe({
      next: () =>
        this.router.navigateByUrl(this.rutaRetorno ?? this.authService.rutaInicio(), { replaceUrl: true }),
      error: () => this.router.navigateByUrl('/login', { replaceUrl: true }),
    });
  }
}
