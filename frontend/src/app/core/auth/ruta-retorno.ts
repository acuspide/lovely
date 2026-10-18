/**
 * RF-04.5: a dónde volver después de iniciar sesión o registrarse (por ejemplo,
 * a confirmar el pedido). Solo se aceptan rutas internas de la app: así nadie
 * puede armar un enlace de login que lleve a un sitio externo.
 */
export function rutaRetornoSegura(valor: string | null | undefined): string | null {
  if (!valor || !valor.startsWith('/') || valor.startsWith('//') || valor.includes('://')) {
    return null;
  }
  return valor;
}

/** Nombre del parámetro de la URL que lleva la ruta de retorno. */
export const PARAMETRO_RETORNO = 'volverA';
