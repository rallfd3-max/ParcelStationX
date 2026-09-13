package com.parcelstationx.api.error;

public final class BadRequestException extends ApiException {
  public BadRequestException(String message, String code) {
    super(400, message, code);
  }
}
