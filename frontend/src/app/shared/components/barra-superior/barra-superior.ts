import { Component, computed, inject, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';
import { CarritoService } from '../../../core/carrito/carrito.service';
import { ETIQUETAS_ROL, RolUsuario } from '../../../core/models/rol-usuario';

interface EnlaceNavegacion {
  ruta: string;
  etiqueta: string;
}

/**
 * RNF-06: cada rol solo ve en la navegación las pantallas de su rol. (Las
 * rutas además están protegidas con guards, y la API con @PreAuthorize.)
 */
const NAVEGACION_POR_ROL: Record<RolUsuario, EnlaceNavegacion[]> = {
  CLIENTE: [{ ruta: '/', etiqueta: 'Catálogo' }],
  ADMINISTRADORA: [
    { ruta: '/dashboard', etiqueta: 'Dashboard' },
    { ruta: '/admin/usuarios', etiqueta: 'Personal' },
    { ruta: '/admin/articulos', etiqueta: 'Productos' },
  ],
  ASESORA_VENTAS: [{ ruta: '/pedidos', etiqueta: 'Pedidos' }],
  ENCARGADA_INVENTARIO: [{ ruta: '/inventario', etiqueta: 'Inventario' }],
};

@Component({
  selector: 'app-barra-superior',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './barra-superior.html',
  styleUrl: './barra-superior.scss',
})
export class BarraSuperior {
  private readonly authService = inject(AuthService);

  /** Sección actual, se muestra junto a la marca. */
  readonly seccion = input<string>('');

  protected readonly usuario = this.authService.usuarioActual;
  /** RF-04.4: contador de unidades del carrito. */
  protected readonly unidadesCarrito = inject(CarritoService).unidades;
  /** El carrito es para visitantes y clientas; el personal no compra desde la tienda. */
  protected readonly muestraCarrito = computed(() => {
    const usuario = this.usuario();
    return usuario === null || usuario.rol === 'CLIENTE';
  });
  protected readonly etiquetasRol = ETIQUETAS_ROL;
  protected readonly rutaInicio = computed(() => (this.usuario() ? this.authService.rutaInicio() : '/'));
  protected readonly enlaces = computed(() => {
    const usuario = this.usuario();
    return usuario ? NAVEGACION_POR_ROL[usuario.rol] : [];
  });

  cerrarSesion(): void {
    this.authService.cerrarSesion('cerrada');
  }
}
