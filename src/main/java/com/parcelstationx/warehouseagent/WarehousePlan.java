package com.parcelstationx.warehouseagent;

import com.parcelstationx.service.ShelfCreationResult;
import java.time.Instant;
import java.util.UUID;

public record WarehousePlan(
    UUID id,
    long userId,
    Instant createdAt,
    Instant expiresAt,
    WarehousePlanStatus status,
    WarehouseAgentAction action,
    ShelfCreationResult preview) {}
