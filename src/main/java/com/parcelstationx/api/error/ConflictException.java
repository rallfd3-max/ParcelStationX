package com.parcelstationx.api.error;

public final class ConflictException extends ApiException {
  public ConflictException(String message, String code) {
    super(409, message, code);
  }
}
