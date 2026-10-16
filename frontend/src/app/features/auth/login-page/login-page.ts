import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';
import { extraerMensajeError } from '../../../core/http/mensaje-error';
import { RUTA_INICIO_POR_ROL } from '../../../core/models/rol-usuario';
import { VerContrasena } from '../../../shared/directives/ver-contrasena';

interface LoginForm {
  email: FormControl<string>;
  contrasena: FormControl<string>;
}

/** Mensaje informativo según cómo terminó la sesión anterior o qué acción se completó. */
const MENSAJES_POR_MOTIVO: Record<string, string> = {
  cerrada: 'Cerraste sesión correctamente.',
  expirada: 'Tu sesión se cerró por inactividad. Inicia sesión de nuevo para continuar.',
  restablecida: 'Tu contraseña se actualizó. Ya puedes iniciar sesión con la nueva.',
};

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, RouterLink, VerContrasena],
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss',
})
export class LoginPage {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly enviando = signal(false);
  protected readonly errorGeneral = signal<string | null>(null);
  protected readonly mensajeInformativo = signal<string | null>(
    MENSAJES_POR_MOTIVO[inject(ActivatedRoute).snapshot.queryParamMap.get('sesion') ?? ''] ?? null,
  );

  protected readonly form = new FormGroup<LoginForm>({
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    contrasena: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  enviar(): void {
    if (this.form.invalid || this.enviando()) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.errorGeneral.set(null);
    this.mensajeInformativo.set(null);

    const { email, contrasena } = this.form.getRawValue();

    this.authService.login(email, contrasena).subscribe({
      // RF-01.3: cada rol entra a su propia pantalla de inicio.
      next: (respuesta) => this.router.navigateByUrl(RUTA_INICIO_POR_ROL[respuesta.usuario.rol], { replaceUrl: true }),
      error: (error: unknown) => {
        this.enviando.set(false);
        this.form.controls.contrasena.reset();
        this.errorGeneral.set(extraerMensajeError(error, 'No se pudo iniciar sesión. Intenta de nuevo.'));
      },
    });
  }
}
