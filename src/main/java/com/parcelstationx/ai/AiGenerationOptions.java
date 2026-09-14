package com.parcelstationx.ai;

import java.time.Duration;

/** Per-request limits so short structured features do not inherit a long free-form budget. */
public record AiGenerationOptions(int maxOutputTokens, Duration timeout) {
  public AiGenerationOptions {
    if (maxOutputTokens <= 0 || timeout == null || timeout.isNegative() || timeout.isZero()) {
      throw new IllegalArgumentException("AI generation limits must be positive.");
    }
  }
}
