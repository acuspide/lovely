import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';
import { extraerMensajeError } from '../../../core/http/mensaje-error';

/**
 * RF-01.8: solicitud del enlace de recuperación. El mensaje de confirmación
 * es el mismo exista o no la cuenta, para no revelar qué correos están
 * registrados.
 */
@Component({
  selector: 'app-recuperar-contrasena-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './recuperar-contrasena-page.html',
  styleUrl: './recuperar-contrasena-page.scss',
})
export class RecuperarContrasenaPage {
  private readonly authService = inject(AuthService);

  protected readonly enviando = signal(false);
  protected readonly confirmacion = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);

  protected readonly email = new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] });

  enviar(): void {
    if (this.email.invalid || this.enviando()) {
      this.email.markAsTouched();
      return;
    }

    this.enviando.set(true);
    this.error.set(null);

    this.authService.solicitarRecuperacion(this.email.value).subscribe({
      next: (respuesta) => {
        this.enviando.set(false);
        this.confirmacion.set(respuesta.mensaje);
      },
      error: (error: unknown) => {
        this.enviando.set(false);
        this.error.set(extraerMensajeError(error, 'No se pudo procesar la solicitud. Intenta de nuevo.'));
      },
    });
  }
}
