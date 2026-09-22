import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { ETIQUETAS_ROL, RolUsuario } from '../../../../core/models/rol-usuario';
import { Usuario } from '../../../../core/models/usuario.model';
import { UsuarioAdminService } from '../usuario-admin.service';

interface CrearUsuarioForm {
  nombre: FormControl<string>;
  email: FormControl<string>;
  contrasena: FormControl<string>;
  rol: FormControl<RolUsuario>;
}

@Component({
  selector: 'app-usuarios-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './usuarios-page.html',
  styleUrl: './usuarios-page.scss',
})
export class UsuariosPage {
  private readonly usuarioAdminService = inject(UsuarioAdminService);

  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly cargando = signal(true);
  protected readonly enviandoAlta = signal(false);
  protected readonly errorAlta = signal<string | null>(null);
  protected readonly etiquetasRol = ETIQUETAS_ROL;
  protected readonly rolesDisponibles: RolUsuario[] = ['ASESORA_VENTAS', 'ENCARGADA_INVENTARIO', 'ADMINISTRADORA', 'CLIENTE'];

  protected readonly form = new FormGroup<CrearUsuarioForm>({
    nombre: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    contrasena: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(8)] }),
    rol: new FormControl<RolUsuario>('ASESORA_VENTAS', { nonNullable: true, validators: [Validators.required] }),
  });

  constructor() {
    this.cargarUsuarios();
  }

  private cargarUsuarios(): void {
    this.cargando.set(true);
    this.usuarioAdminService.listar().subscribe({
      next: (usuarios) => {
        this.usuarios.set(usuarios);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false),
    });
  }

  crearUsuario(): void {
    if (this.form.invalid || this.enviandoAlta()) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviandoAlta.set(true);
    this.errorAlta.set(null);

    this.usuarioAdminService.crear(this.form.getRawValue()).subscribe({
      next: (usuario) => {
        this.usuarios.update((actuales) => [...actuales, usuario]);
        this.enviandoAlta.set(false);
        this.form.reset({ nombre: '', email: '', contrasena: '', rol: 'ASESORA_VENTAS' });
      },
      error: (error: unknown) => {
        this.enviandoAlta.set(false);
        this.errorAlta.set(this.extraerMensaje(error));
      },
    });
  }

  alternarEstado(usuario: Usuario): void {
    this.usuarioAdminService.cambiarEstado(usuario.id, !usuario.activo).subscribe({
      next: (actualizado) => {
        this.usuarios.update((actuales) =>
          actuales.map((u) => (u.id === actualizado.id ? actualizado : u)),
        );
      },
    });
  }

  private extraerMensaje(error: unknown): string {
    if (error instanceof HttpErrorResponse && typeof error.error?.detail === 'string') {
      return error.error.detail;
    }
    return 'No se pudo crear el usuario. Intenta de nuevo.';
  }
}
