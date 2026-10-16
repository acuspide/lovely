package com.example.ecommerce.application;

/**
 * Puerto de salida (arquitectura hexagonal) para dibujar la factura en PDF
 * (RF-08.3). El dominio y los casos de uso no dependen de ninguna librería
 * de PDF: la implementación vive en infraestructura (adaptador con OpenPDF).
 */
public interface GeneradorPdfFactura {

    /**
     * @return el archivo PDF completo
     * @throws GeneracionPdfException si la librería falla al armar el documento
     */
    byte[] generar(DocumentoFactura documento);

    /** El PDF no se pudo generar (error técnico, no de reglas del negocio). */
    class GeneracionPdfException extends RuntimeException {
        public GeneracionPdfException(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }
}
