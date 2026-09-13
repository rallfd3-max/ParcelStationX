package com.parcelstationx.model;

import java.time.LocalDateTime;

public record ShelfLayout(
    Long shelfId,
    double positionX,
    double positionY,
    double positionZ,
    double rotationY,
    double width,
    double height,
    double depth,
    int columns,
    int levels,
    LocalDateTime updatedAt)
    implements java.io.Serializable {}
