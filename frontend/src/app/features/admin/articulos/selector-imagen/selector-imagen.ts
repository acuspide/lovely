import { Component, inject, input, output, signal } from '@angular/core';

import { extraerMensajeError } from '../../../../core/http/mensaje-error';
import { UrlImagenPipe } from '../../../../shared/pipes/url-imagen.pipe';
import { ArticuloService } from '../../../catalogo/articulo.service';

/** RF-02.4: mismas reglas que ArchivoImagen en el backend (que es quien decide). */
const TIPOS_PERMITIDOS = ['image/jpeg', 'image/png', 'image/webp'];
const TAMANO_MAXIMO_MB = 2;
const TAMANO_MAXIMO_BYTES = TAMANO_MAXIMO_MB * 1024 * 1024;

/**
 * RF-02.4: selector de la foto de un producto.
 *
 * Flujo: la Administradora elige un archivo de su equipo → se revisa aquí
 * mismo el formato y el tamaño (respuesta inmediata) → se sube al backend →
 * el backend devuelve la URL → el componente la entrega al formulario con
 * `urlCambiada`.
 *
 * CU-01, curso alterno A4: si la subida falla, se muestran dos salidas:
 * "Reintentar" (vuelve a subir el mismo archivo) y "Continuar sin imagen".
 *
 * El componente no guarda el producto: solo resuelve la imagen. Mientras
 * sube, emite `subiendo = true` para que el formulario no se envíe a medias.
 */
@Component({
  selector: 'app-selector-imagen',
  imports: [UrlImagenPipe],
  templateUrl: './selector-imagen.html',
  styleUrl: './selector-imagen.scss',
})
export class SelectorImagen {
  private readonly articuloService = inject(ArticuloService);

  /** URL actual del producto (al editar) o null si no tiene imagen. */
  readonly url = input<string | null>(null);
  readonly urlCambiada = output<string | null>();
  readonly subiendo = output<boolean>();

  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  /** Último archivo elegido, para poder reintentar sin volver a buscarlo. */
  private archivoPendiente: File | null = null;

  protected readonly tamanoMaximoMb = TAMANO_MAXIMO_MB;
  protected readonly tiposAceptados = TIPOS_PERMITIDOS.join(',');

  protected alSeleccionar(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const archivo = campo.files?.[0];
    campo.value = ''; // Permite volver a elegir el mismo archivo después de un error.
    if (!archivo) {
      return;
    }

    const problema = this.validar(archivo);
    if (problema) {
      this.archivoPendiente = null;
      this.error.set(problema);
      return;
    }

    this.archivoPendiente = archivo;
    this.subir();
  }

  /** CU-01 A4: reintentar la subida del mismo archivo. */
  protected reintentar(): void {
    if (this.archivoPendiente) {
      this.subir();
    }
  }

  /** CU-01 A4: continuar sin imagen (también sirve para quitar la imagen actual). */
  protected quitarImagen(): void {
    this.archivoPendiente = null;
    this.error.set(null);
    this.urlCambiada.emit(null);
  }

  protected puedeReintentar(): boolean {
    return this.archivoPendiente !== null;
  }

  private subir(): void {
    const archivo = this.archivoPendiente;
    if (!archivo) {
      return;
    }

    this.error.set(null);
    this.cambiarCargando(true);

    this.articuloService.subirImagen(archivo).subscribe({
      next: ({ url }) => {
        this.archivoPendiente = null;
        this.cambiarCargando(false);
        this.urlCambiada.emit(url);
      },
      error: (err: unknown) => {
        this.cambiarCargando(false);
        this.error.set(extraerMensajeError(err, 'No se pudo subir la imagen.'));
      },
    });
  }

  /** Revisión rápida en el navegador; el backend vuelve a validar (no se confía solo en esto). */
  private validar(archivo: File): string | null {
    if (!TIPOS_PERMITIDOS.includes(archivo.type)) {
      return 'El archivo no es una imagen válida. Usa una imagen JPG, PNG o WEBP.';
    }
    if (archivo.size > TAMANO_MAXIMO_BYTES) {
      return `La imagen pesa demasiado. El tamaño máximo es ${TAMANO_MAXIMO_MB} MB.`;
    }
    return null;
  }

  private cambiarCargando(valor: boolean): void {
    this.cargando.set(valor);
    this.subiendo.emit(valor);
  }
}
