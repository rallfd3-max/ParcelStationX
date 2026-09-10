package com.parcelstationx.model;

import java.time.LocalDateTime;

public record User(
    Long id,
    String username,
    String passwordHash,
    String displayName,
    UserRole role,
    boolean enabled,
    LocalDateTime createdAt,
    LocalDateTime lastLoginAt)
    implements java.io.Serializable {}
