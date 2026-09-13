package com.parcelstationx.api.error;

public final class UnprocessableException extends ApiException {
  public UnprocessableException(String message, String code) {
    super(422, message, code);
  }
}
