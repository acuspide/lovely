import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';

interface RegistroForm {
  nombre: FormControl<string>;
  email: FormControl<string>;
  contrasena: FormControl<string>;
  confirmarContrasena: FormControl<string>;
}

/** Validador a nivel de grupo: espeja la regla del backend (ContrasenasNoCoincidenException). */
function contrasenasCoincidenValidator(grupo: AbstractControl): ValidationErrors | null {
  const contrasena = grupo.get('contrasena')?.value;
  const confirmacion = grupo.get('confirmarContrasena')?.value;
  return contrasena === confirmacion ? null : { contrasenasNoCoinciden: true };
}

@Component({
  selector: 'app-registro-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registro-page.html',
  styleUrl: './registro-page.scss',
})
export class RegistroPage {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly enviando = signal(false);
  protected readonly errorGeneral = signal<string | null>(null);

  protected readonly form = new FormGroup<RegistroForm>(
    {
      nombre: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
      contrasena: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(8)] }),
      confirmarContrasena: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    },
    { validators: contrasenasCoincidenValidator },
  );

  enviar(): void {
    if (this.form.invalid || this.enviando()) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.errorGeneral.set(null);

    const { nombre, email, contrasena, confirmarContrasena } = this.form.getRawValue();

    this.authService.registro(nombre, email, contrasena, confirmarContrasena).subscribe({
      next: () => this.autenticarTrasRegistro(email, contrasena),
      error: (error: unknown) => {
        this.enviando.set(false);
        this.errorGeneral.set(this.extraerMensaje(error));
      },
    });
  }

  private autenticarTrasRegistro(email: string, contrasena: string): void {
    // El registro solo crea la cuenta; se inicia sesión enseguida para que
    // la nueva clienta no tenga que volver a escribir sus datos en /login.
    this.authService.login(email, contrasena).subscribe({
      next: () => this.router.navigateByUrl('/dashboard'),
      error: () => this.router.navigateByUrl('/login'),
    });
  }

  private extraerMensaje(error: unknown): string {
    if (error instanceof HttpErrorResponse && typeof error.error?.detail === 'string') {
      return error.error.detail;
    }
    return 'No se pudo completar el registro. Intenta de nuevo.';
  }
}
