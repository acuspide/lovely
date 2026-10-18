/** RF-04.6: cómo recibe la clienta su pedido (ver TipoEntrega en el backend). */
export type TipoEntrega = 'RETIRO_EN_TIENDA' | 'DOMICILIO' | 'ENVIO';

export interface OpcionEntrega {
  valor: TipoEntrega;
  etiqueta: string;
  descripcion: string;
  requiereDireccion: boolean;
}

export const OPCIONES_ENTREGA: OpcionEntrega[] = [
  {
    valor: 'RETIRO_EN_TIENDA',
    etiqueta: 'Retiro en tienda',
    descripcion: 'Lo recoges en la tienda de Lovely Girl en Armenia.',
    requiereDireccion: false,
  },
  {
    valor: 'DOMICILIO',
    etiqueta: 'Domicilio',
    descripcion: 'Te lo llevamos a tu casa dentro de la ciudad.',
    requiereDireccion: true,
  },
  {
    valor: 'ENVIO',
    etiqueta: 'Envío nacional',
    descripcion: 'Lo enviamos por transportadora a otra ciudad.',
    requiereDireccion: true,
  },
];

/** Datos de contacto y entrega que viajan con el pedido (F-05 los registra). */
export interface DatosEntrega {
  tipo: TipoEntrega;
  nombreContacto: string;
  telefono: string;
  direccion: string | null;
  ciudad: string | null;
  indicaciones: string | null;
}
