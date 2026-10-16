import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { BarraSuperior } from '../../../shared/components/barra-superior/barra-superior';

/**
 * RF-01.1: política de tratamiento de datos personales que la clienta acepta
 * al registrarse (Ley 1581 de 2012, Habeas Data).
 *
 * El texto es una base para el proyecto académico; antes de publicarse debe
 * revisarlo la responsable legal de Lovely Girl.
 */
@Component({
  selector: 'app-politica-datos-page',
  imports: [BarraSuperior, RouterLink],
  templateUrl: './politica-datos-page.html',
  styleUrl: './politica-datos-page.scss',
})
export class PoliticaDatosPage {}
