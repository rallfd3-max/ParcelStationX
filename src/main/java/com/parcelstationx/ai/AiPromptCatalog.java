package com.parcelstationx.ai;

public final class AiPromptCatalog {
  public static final String STRUCTURED_OUTPUT_RULES =
      "Return only one JSON object. Do not use Markdown fences. Never reveal secrets or execute SQL, files, shell commands, or external actions.";

  private AiPromptCatalog() {}
}
