import { Pipe, PipeTransform } from '@angular/core';

import { SERVIDOR_URL } from '../../core/config/api.config';

/**
 * RF-02.4: convierte la URL guardada en un producto en una que el navegador
 * pueda cargar.
 *
 * - Imágenes subidas al backend: se guardan como ruta relativa
 *   (`/imagenes/articulos/abc.jpg`) y aquí se les antepone la dirección del
 *   servidor. Guardarlas relativas permite cambiar de servidor sin tocar la BD.
 * - Imágenes externas (`https://…`): se usan tal cual.
 * - Sin imagen: devuelve `null`, para que la vista muestre "Sin imagen".
 *
 * @example <img [src]="articulo.imagenUrl | urlImagen" />
 */
@Pipe({ name: 'urlImagen' })
export class UrlImagenPipe implements PipeTransform {
  transform(url: string | null | undefined): string | null {
    if (!url) {
      return null;
    }
    return url.startsWith('/') ? `${SERVIDOR_URL}${url}` : url;
  }
}
