import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, FormGroupDirective, ReactiveFormsModule, Validators } from '@angular/forms';

import { extraerMensajeError } from '../../../core/http/mensaje-error';
import { ETIQUETAS_ROL } from '../../../core/models/rol-usuario';
import { Usuario } from '../../../core/models/usuario.model';
import { BarraSuperior } from '../../../shared/components/barra-superior/barra-superior';
import { RequisitosContrasena } from '../../../shared/components/requisitos-contrasena/requisitos-contrasena';
import { camposCoincidenValidator, contrasenaSeguraValidator } from '../../../shared/validators/cuenta.validators';
import { PerfilService } from '../perfil.service';
import { VerContrasena } from '../../../shared/directives/ver-contrasena';

interface CambioContrasenaForm {
  contrasenaActual: FormControl<string>;
  nuevaContrasena: FormControl<string>;
  confirmarContrasena: FormControl<string>;
}

/** IU-07: perfil de la cuenta, disponible para los cuatro roles. */
@Component({
  selector: 'app-perfil-page',
  imports: [ReactiveFormsModule, BarraSuperior, RequisitosContrasena, VerContrasena],
  templateUrl: './perfil-page.html',
  styleUrl: './perfil-page.scss',
})
export class PerfilPage {
  private readonly perfilService = inject(PerfilService);

  protected readonly perfil = signal<Usuario | null>(null);
  protected readonly etiquetasRol = ETIQUETAS_ROL;

  protected readonly guardando = signal(false);
  protected readonly exito = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);

  protected readonly form = new FormGroup<CambioContrasenaForm>(
    {
      contrasenaActual: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      nuevaContrasena: new FormControl('', { nonNullable: true, validators: [Validators.required, contrasenaSeguraValidator] }),
      confirmarContrasena: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    },
    { validators: camposCoincidenValidator('nuevaContrasena', 'confirmarContrasena') },
  );

  protected readonly contrasenaEscrita = toSignal(this.form.controls.nuevaContrasena.valueChanges, { initialValue: '' });

  constructor() {
    this.perfilService.obtener().subscribe({ next: (perfil) => this.perfil.set(perfil) });
  }

  cambiarContrasena(formulario: FormGroupDirective): void {
    if (this.form.invalid || this.guardando()) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.exito.set(null);
    this.error.set(null);

    const { contrasenaActual, nuevaContrasena, confirmarContrasena } = this.form.getRawValue();

    this.perfilService.cambiarContrasena(contrasenaActual, nuevaContrasena, confirmarContrasena).subscribe({
      next: (respuesta) => {
        this.guardando.set(false);
        this.exito.set(respuesta.mensaje);
        formulario.resetForm();
      },
      error: (error: unknown) => {
        this.guardando.set(false);
        this.error.set(extraerMensajeError(error, 'No se pudo cambiar la contraseña. Intenta de nuevo.'));
      },
    });
  }
}
