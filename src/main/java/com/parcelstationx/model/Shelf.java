package com.parcelstationx.model;

import java.time.LocalDateTime;

public record Shelf(
    Long id,
    String shelfCode,
    String zone,
    int capacity,
    int occupied,
    ShelfStatus status,
    LocalDateTime createdAt)
    implements java.io.Serializable {}
