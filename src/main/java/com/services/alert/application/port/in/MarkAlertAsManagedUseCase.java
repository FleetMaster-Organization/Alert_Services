package com.services.alert.application.port.in;

import java.util.UUID;

public interface MarkAlertAsManagedUseCase {
    void manage(UUID alertId, String managedBy);
}
