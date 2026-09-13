package com.parcelstationx.api.http;

public record ApiResponse<T>(boolean success, T data, String message, String code) {
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, data, null, null);
  }

  public static ApiResponse<Void> failure(String message, String code) {
    return new ApiResponse<>(false, null, message, code);
  }
}
