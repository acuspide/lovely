import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';
import { RolUsuario } from '../../../core/models/rol-usuario';
import { Usuario } from '../../../core/models/usuario.model';

export interface CrearUsuarioInternoPayload {
  nombre: string;
  email: string;
  contrasena: string;
  rol: RolUsuario;
}

/**
 * Cliente HTTP del panel de administración (/api/usuarios, protegido en el
 * backend con @PreAuthorize("hasRole('ADMINISTRADORA')")). Separado de
 * AuthService porque son responsabilidades distintas (SRP): uno gestiona la
 * sesión propia, este gestiona las cuentas de otras personas.
 */
@Injectable({ providedIn: 'root' })
export class UsuarioAdminService {
  private readonly http = inject(HttpClient);

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${API_BASE_URL}/usuarios`);
  }

  crear(payload: CrearUsuarioInternoPayload): Observable<Usuario> {
    return this.http.post<Usuario>(`${API_BASE_URL}/usuarios`, payload);
  }

  cambiarEstado(id: number, activo: boolean): Observable<Usuario> {
    return this.http.patch<Usuario>(`${API_BASE_URL}/usuarios/${id}/estado`, { activo });
  }
}
