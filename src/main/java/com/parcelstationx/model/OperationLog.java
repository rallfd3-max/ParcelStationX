package com.parcelstationx.model;
import java.time.LocalDateTime;
public record OperationLog(Long id, Long userId, String operationType, String targetType, Long targetId, String description, LocalDateTime createdAt) { }
