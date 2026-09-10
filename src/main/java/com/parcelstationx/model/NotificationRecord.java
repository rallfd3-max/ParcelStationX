package com.parcelstationx.model;

import java.time.LocalDateTime;

public record NotificationRecord(
    Long id,
    Long parcelId,
    Long customerId,
    String notificationType,
    String target,
    String content,
    NotificationStatus status,
    int retryCount,
    LocalDateTime createdAt,
    LocalDateTime sentAt,
    String errorMessage)
    implements java.io.Serializable {}
