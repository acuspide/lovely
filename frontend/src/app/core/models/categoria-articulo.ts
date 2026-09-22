/** Espejo del enum CategoriaArticulo del backend (RF-02/RF-03). */
export type CategoriaArticulo = 'FACIAL' | 'CORPORAL' | 'CAPILAR' | 'MAQUILLAJE';

export const ETIQUETAS_CATEGORIA: Record<CategoriaArticulo, string> = {
  FACIAL: 'Facial',
  CORPORAL: 'Corporal',
  CAPILAR: 'Capilar',
  MAQUILLAJE: 'Maquillaje',
};

export const CATEGORIAS: CategoriaArticulo[] = ['FACIAL', 'CORPORAL', 'CAPILAR', 'MAQUILLAJE'];
