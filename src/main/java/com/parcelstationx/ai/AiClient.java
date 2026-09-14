package com.parcelstationx.ai;

public interface AiClient {
  String generate(String systemPrompt, String userPrompt);

  default String generate(String systemPrompt, String userPrompt, AiGenerationOptions options) {
    return generate(systemPrompt, userPrompt);
  }
}
