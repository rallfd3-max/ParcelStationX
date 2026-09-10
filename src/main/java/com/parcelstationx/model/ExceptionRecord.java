package com.parcelstationx.model;

import java.time.LocalDateTime;

public record ExceptionRecord(
    Long id,
    Long parcelId,
    ExceptionType exceptionType,
    String description,
    String status,
    Long createdBy,
    Long handledBy,
    LocalDateTime createdAt,
    LocalDateTime handledAt,
    String resolution) {}
