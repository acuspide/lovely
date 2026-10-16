import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, map, of } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { LineaCarrito, ResultadoCambioCarrito, RevisionCarrito } from './carrito.model';

const CLAVE_STORAGE = 'lovely-girl.carrito.v1';

/**
 * F-04: carrito de compras de la clienta.
 *
 * Dónde vive (RF-04.4): en el navegador (localStorage). Así funciona sin
 * iniciar sesión, como permite el CU-02, y no se pierde al iniciar sesión
 * para confirmar (RF-04.5). También se sincroniza entre pestañas.
 *
 * Qué guarda: solo producto y cantidad. Cada vez que se agrega o cambia algo,
 * se consulta al backend (`POST /api/carrito/revision`), que responde con el
 * precio y el stock actuales (RF-04.1 / CO-01) y ajusta lo que no alcance
 * (RF-04.2). El navegador nunca decide precios ni existencias.
 */
@Injectable({ providedIn: 'root' })
export class CarritoService {
  private readonly http = inject(HttpClient);

  private readonly lineasInternas = signal<LineaCarrito[]>(this.leerGuardado());

  /** Productos del carrito (solo id y cantidad). */
  readonly lineas = this.lineasInternas.asReadonly();
  /** RF-04.4: unidades totales, para el contador de la barra superior. */
  readonly unidades = computed(() => this.lineasInternas().reduce((total, l) => total + l.cantidad, 0));
  readonly estaVacio = computed(() => this.lineasInternas().length === 0);

  constructor() {
    // Si la clienta tiene la tienda abierta en otra pestaña, el carrito se mantiene igual en ambas.
    if (typeof window !== 'undefined') {
      window.addEventListener('storage', (evento) => {
        if (evento.key === CLAVE_STORAGE) {
          this.lineasInternas.set(this.leerGuardado());
        }
      });
    }
  }

  /** Unidades de un producto que ya están en el carrito (0 si no está). */
  cantidadDe(articuloId: number): number {
    return this.lineasInternas().find((l) => l.articuloId === articuloId)?.cantidad ?? 0;
  }

  /**
   * RF-04.1 / RF-04.2: suma `cantidad` unidades del producto, verificando antes
   * con el backend. Si está agotado o desactivado no se agrega; si no alcanza,
   * queda con el máximo disponible.
   */
  agregar(articuloId: number, cantidad = 1): Observable<ResultadoCambioCarrito> {
    return this.fijarCantidad(articuloId, this.cantidadDe(articuloId) + cantidad);
  }

  /** RF-04.3: cambia la cantidad de un producto (mismas verificaciones que al agregar). */
  cambiarCantidad(articuloId: number, cantidad: number): Observable<ResultadoCambioCarrito> {
    return this.fijarCantidad(articuloId, cantidad);
  }

  /** RF-04.3: quita un producto del carrito. */
  eliminar(articuloId: number): void {
    this.guardar(this.lineasInternas().filter((l) => l.articuloId !== articuloId));
  }

  /** RF-04.3: vacía el carrito (también lo usará F-05 después de registrar el pedido). */
  vaciar(): void {
    this.guardar([]);
  }

  /**
   * RF-04.3 / RF-04.7: revisa todo el carrito con el inventario actual y lo
   * deja coherente: quita lo agotado o retirado y baja lo que no alcanza.
   * Devuelve la revisión para que la pantalla muestre los avisos.
   */
  revisar(): Observable<RevisionCarrito> {
    const lineas = this.lineasInternas();
    if (lineas.length === 0) {
      return of({ lineas: [], total: 0, unidades: 0, requiereAtencion: false, tieneProductosComprables: false });
    }
    return this.consultar(lineas).pipe(
      map((revision) => {
        this.guardar(
          revision.lineas
            .filter((l) => l.cantidad > 0)
            .map((l) => ({ articuloId: l.articuloId, cantidad: l.cantidad })),
        );
        return revision;
      }),
    );
  }

  private fijarCantidad(articuloId: number, cantidad: number): Observable<ResultadoCambioCarrito> {
    const cantidadValida = Math.max(1, Math.floor(cantidad)); // RN19: entero mayor a cero
    return this.consultar([{ articuloId, cantidad: cantidadValida }]).pipe(
      map((revision) => {
        const linea = revision.lineas[0];
        const aplicado = linea.cantidad > 0;
        if (aplicado) {
          this.reemplazar(articuloId, linea.cantidad);
        } else {
          this.eliminar(articuloId);
        }
        return { aplicado, cantidad: linea.cantidad, mensaje: linea.mensaje, linea };
      }),
    );
  }

  private consultar(lineas: LineaCarrito[]): Observable<RevisionCarrito> {
    return this.http.post<RevisionCarrito>(`${API_BASE_URL}/carrito/revision`, { lineas });
  }

  private reemplazar(articuloId: number, cantidad: number): void {
    const actuales = this.lineasInternas();
    const existe = actuales.some((l) => l.articuloId === articuloId);
    this.guardar(
      existe
        ? actuales.map((l) => (l.articuloId === articuloId ? { articuloId, cantidad } : l))
        : [...actuales, { articuloId, cantidad }],
    );
  }

  private guardar(lineas: LineaCarrito[]): void {
    this.lineasInternas.set(lineas);
    try {
      localStorage.setItem(CLAVE_STORAGE, JSON.stringify(lineas));
    } catch {
      // Navegación privada o almacenamiento lleno: el carrito sigue funcionando en memoria.
    }
  }

  /** Lee el carrito guardado, descartando cualquier dato con forma inválida. */
  private leerGuardado(): LineaCarrito[] {
    try {
      const crudo = localStorage.getItem(CLAVE_STORAGE);
      const datos: unknown = crudo ? JSON.parse(crudo) : [];
      if (!Array.isArray(datos)) {
        return [];
      }
      return datos.filter(
        (l): l is LineaCarrito =>
          Number.isInteger(l?.articuloId) && Number.isInteger(l?.cantidad) && l.cantidad > 0,
      );
    } catch {
      return [];
    }
  }
}
