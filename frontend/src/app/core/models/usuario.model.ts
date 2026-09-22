import { RolUsuario } from './rol-usuario';

export interface Usuario {
  id: number;
  nombre: string;
  email: string;
  rol: RolUsuario;
  activo: boolean;
}

export interface SesionToken {
  token: string;
  tipo: string;
  expiraEnMinutos: number;
  usuario: Usuario;
}
