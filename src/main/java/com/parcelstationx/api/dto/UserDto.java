package com.parcelstationx.api.dto;

import com.parcelstationx.model.User;

public record UserDto(Long id, String username, String displayName, String role) {
  public static UserDto from(User user) {
    return new UserDto(user.id(), user.username(), user.displayName(), user.role().name());
  }
}
