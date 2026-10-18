import { Component } from '@angular/core';

import { PantallaEnConstruccion } from '../../shared/components/pantalla-en-construccion/pantalla-en-construccion';

/** IU-05: control de inventario, pantalla de inicio de la Encargada de Inventario (RF-01.3). */
@Component({
  selector: 'app-inventario-page',
  imports: [PantallaEnConstruccion],
  template: `
    <app-pantalla-en-construccion
      titulo="Inventario"
      codigo="IU-05"
      descripcion="Aquí controlarás las existencias de los productos."
    />
  `,
})
export class InventarioPage {}
