package com.parcelstationx.ai;

public final class AiAssistantService {
  private final AiIntentParser parser;
  private final AiQueryExecutor executor;

  public AiAssistantService(AiIntentParser parser, AiQueryExecutor executor) {
    this.parser = parser;
    this.executor = executor;
  }

  public AiQueryExecutor.QueryResponse query(String question) {
    return executor.execute(parser.parse(question));
  }
}
