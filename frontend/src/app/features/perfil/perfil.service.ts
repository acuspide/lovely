import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../core/config/api.config';
import { RespuestaMensaje, Usuario } from '../../core/models/usuario.model';

/** Cliente HTTP del perfil de la cuenta con sesión iniciada (IU-07). */
@Injectable({ providedIn: 'root' })
export class PerfilService {
  private readonly http = inject(HttpClient);

  obtener(): Observable<Usuario> {
    return this.http.get<Usuario>(`${API_BASE_URL}/perfil`);
  }

  /** RF-01.8: cambio de contraseña pidiendo la actual. */
  cambiarContrasena(contrasenaActual: string, nuevaContrasena: string, confirmarContrasena: string): Observable<RespuestaMensaje> {
    return this.http.put<RespuestaMensaje>(`${API_BASE_URL}/perfil/contrasena`, {
      contrasenaActual,
      nuevaContrasena,
      confirmarContrasena,
    });
  }
}
