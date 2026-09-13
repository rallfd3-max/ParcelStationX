package com.parcelstationx.api.dto;

public record CreateUserRequest(
    String username, String password, String displayName, String role) {}
