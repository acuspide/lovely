package com.example.ecommerce.domain.valueobject;

/**
 * RF-09.2: medios con los que la clienta paga en la versión 1.0. El sistema
 * no procesa pagos en línea: el dinero llega por fuera del sistema y el
 * personal confirma que lo recibió.
 */
public enum MedioPago {

    /** Transferencia o consignación a la cuenta de Lovely Girl (incluye Nequi y Daviplata). */
    TRANSFERENCIA,
    /** Efectivo recibido en la tienda o al entregar el pedido. */
    EFECTIVO;

    /** RF-09.2: en una transferencia la referencia (número de la transacción) es obligatoria. */
    public boolean exigeReferencia() {
        return this == TRANSFERENCIA;
    }
}
