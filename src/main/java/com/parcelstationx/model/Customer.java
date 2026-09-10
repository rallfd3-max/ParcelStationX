package com.parcelstationx.model;

import java.time.LocalDateTime;

public record Customer(
    Long id,
    String name,
    String mobile,
    String building,
    String room,
    String remark,
    LocalDateTime createdAt,
    LocalDateTime updatedAt)
    implements java.io.Serializable {}
