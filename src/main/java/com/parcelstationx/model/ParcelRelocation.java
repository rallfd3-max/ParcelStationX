package com.parcelstationx.model;

import java.time.LocalDateTime;

public record ParcelRelocation(
    Long id,
    Long parcelId,
    Long fromSlotId,
    Long newSlotId,
    Long operatorId,
    String reason,
    LocalDateTime createdAt)
    implements java.io.Serializable {}
