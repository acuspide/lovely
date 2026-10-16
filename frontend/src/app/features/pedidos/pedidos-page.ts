import { Component } from '@angular/core';

import { PantallaEnConstruccion } from '../../shared/components/pantalla-en-construccion/pantalla-en-construccion';

/** IU-04: gestión de pedidos, pantalla de inicio de la Asesora de Ventas (RF-01.3). */
@Component({
  selector: 'app-pedidos-page',
  imports: [PantallaEnConstruccion],
  template: `
    <app-pantalla-en-construccion
      titulo="Pedidos"
      codigo="IU-04"
      descripcion="Aquí gestionarás los pedidos de las clientas."
    />
  `,
})
export class PedidosPage {}
