import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';
import { extraerMensajeError } from '../../../core/http/mensaje-error';
import { RequisitosContrasena } from '../../../shared/components/requisitos-contrasena/requisitos-contrasena';
import { camposCoincidenValidator, contrasenaSeguraValidator } from '../../../shared/validators/cuenta.validators';
import { VerContrasena } from '../../../shared/directives/ver-contrasena';

interface RestablecerForm {
  nuevaContrasena: FormControl<string>;
  confirmarContrasena: FormControl<string>;
}

/**
 * RF-01.8: pantalla a la que lleva el enlace del correo. El token se lee de la
 * URL y se quita enseguida de la barra de direcciones, para que no quede en el
 * historial del navegador.
 */
@Component({
  selector: 'app-restablecer-contrasena-page',
  imports: [ReactiveFormsModule, RouterLink, RequisitosContrasena, VerContrasena],
  templateUrl: './restablecer-contrasena-page.html',
  styleUrl: './restablecer-contrasena-page.scss',
})
export class RestablecerContrasenaPage {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  private readonly token = inject(ActivatedRoute).snapshot.queryParamMap.get('token');

  protected readonly tieneToken = this.token !== null && this.token.length > 0;
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = new FormGroup<RestablecerForm>(
    {
      nuevaContrasena: new FormControl('', { nonNullable: true, validators: [Validators.required, contrasenaSeguraValidator] }),
      confirmarContrasena: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    },
    { validators: camposCoincidenValidator('nuevaContrasena', 'confirmarContrasena') },
  );

  protected readonly contrasenaEscrita = toSignal(this.form.controls.nuevaContrasena.valueChanges, { initialValue: '' });

  constructor() {
    if (this.tieneToken) {
      this.router.navigate([], { queryParams: {}, replaceUrl: true });
    }
  }

  enviar(): void {
    if (!this.token || this.form.invalid || this.enviando()) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.error.set(null);

    const { nuevaContrasena, confirmarContrasena } = this.form.getRawValue();

    this.authService.restablecerContrasena(this.token, nuevaContrasena, confirmarContrasena).subscribe({
      next: () => this.router.navigate(['/login'], { queryParams: { sesion: 'restablecida' }, replaceUrl: true }),
      error: (error: unknown) => {
        this.enviando.set(false);
        this.error.set(extraerMensajeError(error, 'No se pudo actualizar la contraseña. Intenta de nuevo.'));
      },
    });
  }
}
