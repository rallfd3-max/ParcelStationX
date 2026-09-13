package com.parcelstationx.api.dto;

import com.parcelstationx.model.User;

public record UserDto(
    Long id, String username, String displayName, String role, boolean enabled, String createdAt) {
  public static UserDto from(User user) {
    return new UserDto(
        user.id(),
        user.username(),
        user.displayName(),
        user.role().name(),
        user.enabled(),
        user.createdAt() == null ? null : user.createdAt().toString());
  }
}
