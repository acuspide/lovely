import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { RUTA_INICIO_POR_ROL, RolUsuario } from '../models/rol-usuario';
import {
  RespuestaLogin,
  RespuestaMensaje,
  RespuestaRenovacion,
  SolicitudRegistro,
  Usuario,
} from '../models/usuario.model';

const CLAVE_STORAGE = 'lovely_girl_sesion';

/**
 * Sesión guardada en el navegador. `expiraEnMs` se calcula con el reloj local
 * a partir de los segundos que informa el servidor, así el aviso de RF-01.6
 * no depende de que el reloj del equipo esté en hora.
 */
interface SesionLocal {
  token: string;
  usuario: Usuario;
  expiraEnMs: number;
}

/** Motivo con el que termina una sesión; la pantalla de login muestra un mensaje distinto para cada uno. */
export type MotivoCierreSesion = 'cerrada' | 'expirada';

/**
 * Único punto de la app que sabe cómo se guarda la sesión y cómo se habla con
 * el backend de autenticación. El resto de la app solo lee sus signals o
 * llama a sus métodos (SRP).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly sesion = signal<SesionLocal | null>(this.leerSesionGuardada());

  readonly usuarioActual = computed<Usuario | null>(() => this.sesion()?.usuario ?? null);
  readonly estaAutenticado = computed(() => this.sesion() !== null);
  /** Momento (ms desde epoch, reloj local) en que vence la sesión si no hay actividad. */
  readonly expiraEnMs = computed(() => this.sesion()?.expiraEnMs ?? null);

  constructor() {
    // Mantiene sincronizadas las pestañas abiertas: cerrar sesión o renovarla
    // en una se refleja en todas.
    window.addEventListener('storage', (evento) => {
      if (evento.key === CLAVE_STORAGE) {
        this.sincronizarDesdeOtraPestana();
      }
    });
  }

  // ---------------------------------------------------------------------
  // Inicio y cierre de sesión (RF-01.3, RF-01.6, RF-01.7)
  // ---------------------------------------------------------------------

  login(email: string, contrasena: string): Observable<RespuestaLogin> {
    return this.http.post<RespuestaLogin>(`${API_BASE_URL}/auth/login`, { email, contrasena }).pipe(
      tap((respuesta) =>
        this.guardarSesion({
          token: respuesta.token,
          usuario: respuesta.usuario,
          expiraEnMs: this.calcularExpiracion(respuesta.segundosParaExpirar),
        }),
      ),
    );
  }

  /**
   * RF-01.7: anula la sesión en el servidor y en el navegador, y lleva a la
   * pantalla de inicio de sesión reemplazando la entrada del historial.
   * Si la sesión ya venció (RF-01.6) no hace falta avisarle al servidor.
   */
  cerrarSesion(motivo: MotivoCierreSesion = 'cerrada'): void {
    const token = this.obtenerToken();
    if (!token) {
      return;
    }

    if (motivo === 'cerrada') {
      // El token se envía explícito porque la sesión local se borra enseguida.
      this.http
        .post<void>(`${API_BASE_URL}/auth/logout`, {}, { headers: { Authorization: `Bearer ${token}` } })
        .subscribe({ error: () => undefined });
    }

    this.borrarSesion();
    this.router.navigate(['/login'], { queryParams: { sesion: motivo }, replaceUrl: true });
  }

  /** RF-01.6: "Seguir conectado". La respuesta trae la nueva vigencia. */
  renovarSesion(): Observable<RespuestaRenovacion> {
    return this.http.post<RespuestaRenovacion>(`${API_BASE_URL}/auth/sesion/renovar`, {});
  }

  /** Registra la vigencia que informó el servidor en cualquier respuesta autenticada. */
  registrarVigencia(segundosParaExpirar: number): void {
    const actual = this.sesion();
    if (actual) {
      this.guardarSesion({ ...actual, expiraEnMs: this.calcularExpiracion(segundosParaExpirar) });
    }
  }

  // ---------------------------------------------------------------------
  // Registro y recuperación de contraseña (RF-01.1, RF-01.8)
  // ---------------------------------------------------------------------

  registro(solicitud: SolicitudRegistro): Observable<Usuario> {
    return this.http.post<Usuario>(`${API_BASE_URL}/auth/registro`, solicitud);
  }

  solicitarRecuperacion(email: string): Observable<RespuestaMensaje> {
    return this.http.post<RespuestaMensaje>(`${API_BASE_URL}/auth/recuperar-contrasena`, { email });
  }

  restablecerContrasena(token: string, nuevaContrasena: string, confirmarContrasena: string): Observable<RespuestaMensaje> {
    return this.http.post<RespuestaMensaje>(`${API_BASE_URL}/auth/restablecer-contrasena`, {
      token,
      nuevaContrasena,
      confirmarContrasena,
    });
  }

  // ---------------------------------------------------------------------
  // Consultas
  // ---------------------------------------------------------------------

  obtenerToken(): string | null {
    return this.sesion()?.token ?? null;
  }

  tieneAlgunRol(roles: RolUsuario[]): boolean {
    const usuario = this.usuarioActual();
    return usuario !== null && roles.includes(usuario.rol);
  }

  /** RF-01.3: pantalla de inicio del rol de la sesión actual. */
  rutaInicio(): string {
    const usuario = this.usuarioActual();
    return usuario ? RUTA_INICIO_POR_ROL[usuario.rol] : '/';
  }

  // ---------------------------------------------------------------------
  // Persistencia local
  // ---------------------------------------------------------------------

  private calcularExpiracion(segundosParaExpirar: number): number {
    return Date.now() + segundosParaExpirar * 1000;
  }

  private guardarSesion(sesion: SesionLocal): void {
    this.escribirStorage(() => localStorage.setItem(CLAVE_STORAGE, JSON.stringify(sesion)));
    this.sesion.set(sesion);
  }

  private borrarSesion(): void {
    this.escribirStorage(() => localStorage.removeItem(CLAVE_STORAGE));
    this.sesion.set(null);
  }

  private sincronizarDesdeOtraPestana(): void {
    const sesion = this.leerSesionGuardada();
    const habiaSesion = this.sesion() !== null;
    this.sesion.set(sesion);

    if (habiaSesion && sesion === null) {
      this.router.navigate(['/login'], { replaceUrl: true });
    }
  }

  /** Descarta la sesión guardada si ya venció (por ejemplo, al volver a abrir el navegador). */
  private leerSesionGuardada(): SesionLocal | null {
    try {
      const crudo = localStorage.getItem(CLAVE_STORAGE);
      const sesion = crudo ? (JSON.parse(crudo) as SesionLocal) : null;
      return sesion && sesion.expiraEnMs > Date.now() ? sesion : null;
    } catch {
      return null;
    }
  }

  private escribirStorage(operacion: () => void): void {
    try {
      operacion();
    } catch {
      // Sin almacenamiento (modo privado estricto): la sesión vive solo en memoria.
    }
  }
}
