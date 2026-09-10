package com.parcelstationx.model;

import java.time.LocalDateTime;

public record ParcelEvent(
    Long id,
    Long parcelId,
    String eventType,
    ParcelStatus fromStatus,
    ParcelStatus toStatus,
    Long operatorId,
    String description,
    LocalDateTime createdAt)
    implements java.io.Serializable {}
