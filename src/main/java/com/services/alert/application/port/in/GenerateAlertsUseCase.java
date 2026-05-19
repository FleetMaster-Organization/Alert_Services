package com.services.alert.application.port.in;

public interface GenerateAlertsUseCase {

    /**
     * Consulta a Vehicle y Driver Service por documentos próximos a vencer
     * y genera las alertas faltantes, respetando el anti-duplicado por
     * (entityId, documentId, status=PENDING).
     *
     * @return cantidad de alertas nuevas creadas.
     */
    int generate();
}
