package com.parcelstationx.model;

import java.time.LocalDateTime;

public record ShelfSlot(
    Long id,
    Long shelfId,
    String slotCode,
    int levelIndex,
    int columnIndex,
    boolean enabled,
    LocalDateTime createdAt)
    implements java.io.Serializable {}
