import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { AvisoSesion } from './core/auth/aviso-sesion/aviso-sesion';
import { InactividadSesionService } from './core/auth/inactividad-sesion.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, AvisoSesion],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  constructor() {
    // RF-01.6: vigila la inactividad en toda la aplicación.
    inject(InactividadSesionService).iniciar();
  }
}
