package com.parcelstationx.service;

import java.security.SecureRandom;
import java.util.Set;

public final class PickupCodeGenerator {
  private final SecureRandom random = new SecureRandom();

  public String generate(Set<String> activeCodes) {
    for (int attempt = 0; attempt < 100; attempt++) {
      String code = "%06d".formatted(random.nextInt(1_000_000));
      if (!activeCodes.contains(code)) return code;
    }
    throw new IllegalStateException("Could not generate a unique pickup code.");
  }
}
