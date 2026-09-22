import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { RolUsuario } from '../models/rol-usuario';
import { SesionToken, Usuario } from '../models/usuario.model';

const CLAVE_STORAGE = 'lovely_girl_sesion';

/**
 * Único punto de la app que conoce cómo se guarda la sesión (localStorage) y
 * cómo se llama al backend de autenticación. El resto de la app solo lee el
 * signal `usuarioActual` o llama a login/registro/logout (SRP).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly sesion = signal<SesionToken | null>(this.leerSesionGuardada());

  readonly usuarioActual = computed<Usuario | null>(() => this.sesion()?.usuario ?? null);
  readonly estaAutenticado = computed(() => this.sesion() !== null);

  constructor(private readonly http: HttpClient) {}

  login(email: string, contrasena: string): Observable<SesionToken> {
    return this.http.post<SesionToken>(`${API_BASE_URL}/auth/login`, { email, contrasena }).pipe(
      tap((sesion) => this.guardarSesion(sesion)),
    );
  }

  registro(nombre: string, email: string, contrasena: string, confirmarContrasena: string): Observable<Usuario> {
    return this.http.post<Usuario>(`${API_BASE_URL}/auth/registro`, {
      nombre,
      email,
      contrasena,
      confirmarContrasena,
    });
  }

  logout(): void {
    localStorage.removeItem(CLAVE_STORAGE);
    this.sesion.set(null);
  }

  obtenerToken(): string | null {
    return this.sesion()?.token ?? null;
  }

  tieneAlgunRol(roles: RolUsuario[]): boolean {
    const usuario = this.usuarioActual();
    return usuario !== null && roles.includes(usuario.rol);
  }

  private guardarSesion(sesion: SesionToken): void {
    localStorage.setItem(CLAVE_STORAGE, JSON.stringify(sesion));
    this.sesion.set(sesion);
  }

  private leerSesionGuardada(): SesionToken | null {
    try {
      const crudo = localStorage.getItem(CLAVE_STORAGE);
      return crudo ? (JSON.parse(crudo) as SesionToken) : null;
    } catch {
      return null;
    }
  }
}
