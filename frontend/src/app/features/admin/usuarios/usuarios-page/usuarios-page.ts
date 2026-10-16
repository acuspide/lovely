import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { AuthService } from '../../../../core/auth/auth.service';
import { extraerMensajeError } from '../../../../core/http/mensaje-error';
import { ETIQUETAS_ROL, ROLES_PERSONAL, RolUsuario } from '../../../../core/models/rol-usuario';
import { Usuario } from '../../../../core/models/usuario.model';
import { BarraSuperior } from '../../../../shared/components/barra-superior/barra-superior';
import { DialogoConfirmacion } from '../../../../shared/components/dialogo-confirmacion/dialogo-confirmacion';
import { RequisitosContrasena } from '../../../../shared/components/requisitos-contrasena/requisitos-contrasena';
import { contrasenaSeguraValidator, telefonoValidator } from '../../../../shared/validators/cuenta.validators';
import { UsuarioAdminService } from '../usuario-admin.service';
import { VerContrasena } from '../../../../shared/directives/ver-contrasena';

interface CuentaPersonalForm {
  nombre: FormControl<string>;
  email: FormControl<string>;
  telefono: FormControl<string>;
  contrasena: FormControl<string>;
  rol: FormControl<RolUsuario>;
}

const VALORES_INICIALES = { nombre: '', email: '', telefono: '', contrasena: '', rol: 'ASESORA_VENTAS' as RolUsuario };

/**
 * RF-01.2: la Administradora crea, edita y desactiva las cuentas del personal.
 * Las cuentas no se eliminan (no hay botón de eliminar), solo se desactivan.
 */
@Component({
  selector: 'app-usuarios-page',
  imports: [ReactiveFormsModule, BarraSuperior, RequisitosContrasena, VerContrasena, DialogoConfirmacion],
  templateUrl: './usuarios-page.html',
  styleUrl: './usuarios-page.scss',
})
export class UsuariosPage {
  private readonly usuarioAdminService = inject(UsuarioAdminService);
  private readonly authService = inject(AuthService);

  protected readonly personal = signal<Usuario[]>([]);
  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly errorTabla = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);
  protected readonly cuentaEnEdicion = signal<Usuario | null>(null);
  /** Cuenta que espera confirmación para desactivarse (null = ventana cerrada). */
  protected readonly cuentaPorDesactivar = signal<Usuario | null>(null);

  protected readonly etiquetasRol = ETIQUETAS_ROL;
  protected readonly rolesPersonal = ROLES_PERSONAL;
  protected readonly idUsuarioActual = computed(() => this.authService.usuarioActual()?.id);

  protected readonly form = new FormGroup<CuentaPersonalForm>({
    nombre: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(150)] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    telefono: new FormControl('', { nonNullable: true, validators: [telefonoValidator] }),
    contrasena: new FormControl('', { nonNullable: true, validators: [Validators.required, contrasenaSeguraValidator] }),
    rol: new FormControl<RolUsuario>('ASESORA_VENTAS', { nonNullable: true, validators: [Validators.required] }),
  });

  protected readonly contrasenaEscrita = toSignal(this.form.controls.contrasena.valueChanges, { initialValue: '' });

  constructor() {
    this.cargarPersonal();
  }

  editar(cuenta: Usuario): void {
    this.limpiarMensajes();
    this.cuentaEnEdicion.set(cuenta);
    // La contraseña no se edita aquí: la persona la cambia desde su perfil o
    // con "¿Olvidaste tu contraseña?" (RF-01.8).
    this.form.controls.contrasena.disable();
    this.form.reset({
      nombre: cuenta.nombre,
      email: cuenta.email,
      telefono: cuenta.telefono ?? '',
      contrasena: '',
      rol: cuenta.rol,
    });
  }

  cancelarEdicion(): void {
    this.cuentaEnEdicion.set(null);
    this.form.controls.contrasena.enable();
    this.form.reset(VALORES_INICIALES);
  }

  guardar(): void {
    if (this.form.invalid || this.guardando()) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.limpiarMensajes();

    const { contrasena, ...datos } = this.form.getRawValue();
    const enEdicion = this.cuentaEnEdicion();
    const peticion = enEdicion
      ? this.usuarioAdminService.editar(enEdicion.id, datos)
      : this.usuarioAdminService.crear({ ...datos, contrasena });

    peticion.subscribe({
      next: (cuenta) => {
        this.guardando.set(false);
        this.reemplazarEnLista(cuenta);
        this.exito.set(enEdicion ? `Se actualizó la cuenta de ${cuenta.nombre}.` : `Se creó la cuenta de ${cuenta.nombre}.`);
        this.cancelarEdicion();
      },
      error: (error: unknown) => {
        this.guardando.set(false);
        this.errorFormulario.set(extraerMensajeError(error, 'No se pudo guardar la cuenta. Intenta de nuevo.'));
      },
    });
  }

  /** Activar es inmediato; desactivar pide confirmación porque saca a la persona de su sesión. */
  alternarEstado(cuenta: Usuario): void {
    if (cuenta.activo) {
      this.cuentaPorDesactivar.set(cuenta);
      return;
    }
    this.cambiarEstado(cuenta);
  }

  confirmarDesactivacion(): void {
    const cuenta = this.cuentaPorDesactivar();
    this.cuentaPorDesactivar.set(null);
    if (cuenta) {
      this.cambiarEstado(cuenta);
    }
  }

  private cambiarEstado(cuenta: Usuario): void {
    this.limpiarMensajes();
    this.usuarioAdminService.cambiarEstado(cuenta.id, !cuenta.activo).subscribe({
      next: (actualizada) => this.reemplazarEnLista(actualizada),
      error: (error: unknown) =>
        this.errorTabla.set(extraerMensajeError(error, 'No se pudo cambiar el estado de la cuenta.')),
    });
  }

  private cargarPersonal(): void {
    this.cargando.set(true);
    this.usuarioAdminService.listarPersonal().subscribe({
      next: (personal) => {
        this.personal.set(personal);
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        this.cargando.set(false);
        this.errorTabla.set(extraerMensajeError(error, 'No se pudo cargar el personal.'));
      },
    });
  }

  private reemplazarEnLista(cuenta: Usuario): void {
    this.personal.update((actuales) =>
      actuales.some((u) => u.id === cuenta.id)
        ? actuales.map((u) => (u.id === cuenta.id ? cuenta : u))
        : [...actuales, cuenta].sort((a, b) => a.nombre.localeCompare(b.nombre)),
    );
  }

  private limpiarMensajes(): void {
    this.errorFormulario.set(null);
    this.errorTabla.set(null);
    this.exito.set(null);
  }
}
