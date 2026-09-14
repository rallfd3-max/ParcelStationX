package com.parcelstationx.ai;

public final class FakeAiClient implements AiClient {
  private final String response;
  private final AiException failure;
  private String lastSystemPrompt;
  private String lastUserPrompt;

  public FakeAiClient(String response) {
    this.response = response;
    this.failure = null;
  }

  public FakeAiClient(AiException failure) {
    this.response = null;
    this.failure = failure;
  }

  @Override
  public String generate(String systemPrompt, String userPrompt) {
    lastSystemPrompt = systemPrompt;
    lastUserPrompt = userPrompt;
    if (failure != null) throw failure;
    return response;
  }

  public String lastSystemPrompt() {
    return lastSystemPrompt;
  }

  public String lastUserPrompt() {
    return lastUserPrompt;
  }
}
