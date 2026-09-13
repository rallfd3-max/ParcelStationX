package com.parcelstationx.api.error;

public final class HttpErrorException extends ApiException {
  public HttpErrorException(int status, String message, String code) {
    super(status, message, code);
  }
}
