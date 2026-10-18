import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Validadores de formulario que espejan las reglas del backend de F-01. El
 * backend sigue siendo la autoridad; estos solo dan retroalimentación
 * inmediata mientras se escribe.
 */

export interface RequisitosContrasena {
  longitud: boolean;
  letra: boolean;
  numero: boolean;
  especial: boolean;
}

export const CONTRASENA_LONGITUD_MINIMA = 8;
export const CONTRASENA_LONGITUD_MAXIMA = 20;

/**
 * RF-01.1: entre 8 y 20 caracteres, con al menos una letra, un número y un
 * carácter especial (cualquier símbolo que no sea letra, número ni espacio).
 * Debe coincidir con PoliticaContrasena del backend.
 */
export function evaluarContrasena(valor: string): RequisitosContrasena {
  const caracteres = [...valor].length; // cuenta tildes y emojis como un solo carácter
  return {
    longitud: caracteres >= CONTRASENA_LONGITUD_MINIMA && caracteres <= CONTRASENA_LONGITUD_MAXIMA,
    letra: /\p{L}/u.test(valor),
    numero: /\p{Nd}/u.test(valor),
    especial: /[^\p{L}\p{N}\s]/u.test(valor),
  };
}

export const contrasenaSeguraValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const valor: string = control.value ?? '';
  if (!valor) {
    return null; // De lo vacío se encarga Validators.required.
  }
  const requisitos = evaluarContrasena(valor);
  return requisitos.longitud && requisitos.letra && requisitos.numero && requisitos.especial
    ? null
    : { contrasenaDebil: requisitos };
};

/** Validador de grupo: los dos campos indicados deben tener el mismo valor. */
export function camposCoincidenValidator(campo: string, confirmacion: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const valor = grupo.get(campo)?.value;
    const valorConfirmacion = grupo.get(confirmacion)?.value;
    return !valorConfirmacion || valor === valorConfirmacion ? null : { noCoinciden: true };
  };
}

/** Teléfono colombiano de 10 dígitos (celular 3XX o fijo 60X), con o sin +57, espacios o guiones. */
export const telefonoValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const valor: string = control.value ?? '';
  if (!valor.trim()) {
    return null;
  }
  let digitos = valor.replace(/[\s\-()]/g, '').replace(/^\+/, '');
  if (digitos.length === 12 && digitos.startsWith('57')) {
    digitos = digitos.slice(2);
  }
  return /^[36]\d{9}$/.test(digitos) ? null : { telefonoInvalido: true };
};
