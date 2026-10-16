import { DestroyRef, Injectable, inject, signal } from '@angular/core';

import { AuthService } from './auth.service';

/** RF-01.6: el aviso aparece dos minutos antes del vencimiento. */
const AVISO_ANTES_DE_VENCER_MS = 2 * 60 * 1000;

/** Como máximo una renovación automática por minuto mientras haya actividad. */
const INTERVALO_MINIMO_RENOVACION_MS = 60 * 1000;

const EVENTOS_DE_ACTIVIDAD = ['click', 'keydown', 'scroll', 'touchstart', 'mousemove'] as const;

/**
 * RF-01.6: cierra la sesión tras 30 minutos sin actividad, avisando dos
 * minutos antes con la opción "Seguir conectado".
 *
 * La actividad de la usuaria (clics, teclado, desplazamiento) renueva la
 * sesión en el servidor, como mucho una vez por minuto. Mientras el aviso está
 * visible, solo el botón "Seguir conectado" la renueva: mover el ratón por
 * accidente no debe contar como decisión de seguir conectada.
 */
@Injectable({ providedIn: 'root' })
export class InactividadSesionService {
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  readonly avisoVisible = signal(false);
  readonly segundosRestantes = signal(0);

  private ultimaRenovacionMs = Date.now();
  private renovando = false;
  private iniciado = false;

  iniciar(): void {
    if (this.iniciado) {
      return;
    }
    this.iniciado = true;

    const alHaberActividad = () => this.alHaberActividad();
    EVENTOS_DE_ACTIVIDAD.forEach((evento) =>
      document.addEventListener(evento, alHaberActividad, { passive: true }),
    );
    const temporizador = setInterval(() => this.revisarVencimiento(), 1000);

    this.destroyRef.onDestroy(() => {
      clearInterval(temporizador);
      EVENTOS_DE_ACTIVIDAD.forEach((evento) => document.removeEventListener(evento, alHaberActividad));
    });
  }

  seguirConectado(): void {
    this.renovar();
  }

  cerrarSesionAhora(): void {
    this.avisoVisible.set(false);
    this.authService.cerrarSesion('cerrada');
  }

  private alHaberActividad(): void {
    if (!this.authService.estaAutenticado() || this.avisoVisible()) {
      return;
    }
    if (Date.now() - this.ultimaRenovacionMs >= INTERVALO_MINIMO_RENOVACION_MS) {
      this.renovar();
    }
  }

  private renovar(): void {
    if (this.renovando) {
      return;
    }
    this.renovando = true;
    this.ultimaRenovacionMs = Date.now();

    this.authService.renovarSesion().subscribe({
      next: (respuesta) => {
        this.authService.registrarVigencia(respuesta.segundosParaExpirar);
        this.avisoVisible.set(false);
        this.renovando = false;
      },
      // Un 401 ya lo maneja el interceptor (cierra la sesión); ante un error
      // de red se deja que el temporizador siga su curso.
      error: () => (this.renovando = false),
    });
  }

  private revisarVencimiento(): void {
    const expiraEnMs = this.authService.expiraEnMs();
    if (expiraEnMs === null) {
      this.avisoVisible.set(false);
      return;
    }

    const restanteMs = expiraEnMs - Date.now();
    if (restanteMs <= 0) {
      this.avisoVisible.set(false);
      this.authService.cerrarSesion('expirada');
      return;
    }

    this.segundosRestantes.set(Math.ceil(restanteMs / 1000));
    this.avisoVisible.set(restanteMs <= AVISO_ANTES_DE_VENCER_MS);
  }
}
