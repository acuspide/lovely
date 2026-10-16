import { RolUsuario } from './rol-usuario';

export interface Usuario {
  id: number;
  nombre: string;
  email: string;
  telefono: string | null;
  rol: RolUsuario;
  activo: boolean;
}

/** Respuesta de POST /api/auth/login. */
export interface RespuestaLogin {
  token: string;
  tipo: string;
  segundosParaExpirar: number;
  usuario: Usuario;
}

/** Respuesta de POST /api/auth/sesion/renovar ("Seguir conectado"). */
export interface RespuestaRenovacion {
  segundosParaExpirar: number;
}

export interface RespuestaMensaje {
  mensaje: string;
}

export interface SolicitudRegistro {
  nombre: string;
  email: string;
  telefono: string;
  contrasena: string;
  confirmarContrasena: string;
  aceptaHabeasData: boolean;
}
