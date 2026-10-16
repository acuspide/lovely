import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';
import { BarraSuperior } from '../../../shared/components/barra-superior/barra-superior';

/** IU-03: pantalla de inicio de la Administradora (RF-01.3). */
@Component({
  selector: 'app-dashboard-page',
  imports: [RouterLink, BarraSuperior],
  templateUrl: './dashboard-page.html',
  styleUrl: './dashboard-page.scss',
})
export class DashboardPage {
  protected readonly usuario = inject(AuthService).usuarioActual;
}
