package com.parcelstationx.ai;

public final class AiException extends RuntimeException {
  private final AiErrorCode code;

  public AiException(AiErrorCode code, String message) {
    super(message);
    this.code = code;
  }

  public AiException(AiErrorCode code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  public AiErrorCode code() {
    return code;
  }
}
