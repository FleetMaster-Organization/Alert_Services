package com.services.alert.domain.enums;

/**
 * Clasificación visual de la alerta según los días que restan
 * para el vencimiento del documento (REQ-36).
 *
 * WARNING (Amarillo) → ≤30 días para vencer.
 * EXPIRED (Rojo)     → documento ya vencido.
 *
 * Es un valor CALCULADO en aplicación, NO se persiste en BD,
 * para evitar inconsistencias (mismo criterio aplicado al legal_status
 * de los documentos de vehículos).
 */
public enum AlertCriticality {
    WARNING,
    EXPIRED
}
