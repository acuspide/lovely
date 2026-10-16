import { Component, computed, inject } from '@angular/core';

import { InactividadSesionService } from '../inactividad-sesion.service';

/**
 * RF-01.6: diálogo que avisa, dos minutos antes, que la sesión está por
 * vencer por inactividad y ofrece "Seguir conectado".
 */
@Component({
  selector: 'app-aviso-sesion',
  templateUrl: './aviso-sesion.html',
  styleUrl: './aviso-sesion.scss',
})
export class AvisoSesion {
  protected readonly inactividad = inject(InactividadSesionService);

  protected readonly tiempoRestante = computed(() => {
    const total = this.inactividad.segundosRestantes();
    const minutos = Math.floor(total / 60);
    const segundos = (total % 60).toString().padStart(2, '0');
    return `${minutos}:${segundos}`;
  });
}
