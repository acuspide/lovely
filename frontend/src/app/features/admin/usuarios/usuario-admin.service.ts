import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';
import { RolUsuario } from '../../../core/models/rol-usuario';
import { Usuario } from '../../../core/models/usuario.model';

export interface DatosCuentaPersonal {
  nombre: string;
  email: string;
  telefono: string;
  rol: RolUsuario;
}

export interface NuevaCuentaPersonal extends DatosCuentaPersonal {
  contrasena: string;
}

/**
 * Cliente HTTP de la gestión del personal (RF-01.2), protegida en el backend
 * con @PreAuthorize("hasRole('ADMINISTRADORA')"). Separado de AuthService
 * porque son responsabilidades distintas (SRP): uno gestiona la sesión
 * propia, este gestiona las cuentas de otras personas.
 */
@Injectable({ providedIn: 'root' })
export class UsuarioAdminService {
  private readonly http = inject(HttpClient);

  listarPersonal(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${API_BASE_URL}/usuarios`);
  }

  crear(cuenta: NuevaCuentaPersonal): Observable<Usuario> {
    return this.http.post<Usuario>(`${API_BASE_URL}/usuarios`, cuenta);
  }

  editar(id: number, datos: DatosCuentaPersonal): Observable<Usuario> {
    return this.http.put<Usuario>(`${API_BASE_URL}/usuarios/${id}`, datos);
  }

  cambiarEstado(id: number, activo: boolean): Observable<Usuario> {
    return this.http.patch<Usuario>(`${API_BASE_URL}/usuarios/${id}/estado`, { activo });
  }
}
