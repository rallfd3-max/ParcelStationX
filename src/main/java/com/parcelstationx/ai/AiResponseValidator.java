package com.parcelstationx.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class AiResponseValidator {
  private final ObjectMapper mapper = new ObjectMapper();

  public JsonNode requireObject(String output) {
    if (output == null || output.isBlank()) throw badResponse();
    String normalized = output.trim();
    if (normalized.startsWith("```")) {
      int newline = normalized.indexOf('\n');
      int end = normalized.lastIndexOf("```");
      if (newline > 0 && end > newline) normalized = normalized.substring(newline + 1, end).trim();
    }
    try {
      JsonNode node = mapper.readTree(normalized);
      if (node == null || !node.isObject()) throw badResponse();
      return node;
    } catch (JsonProcessingException exception) {
      throw new AiException(
          AiErrorCode.AI_BAD_RESPONSE, "AI returned an invalid structured response.", exception);
    }
  }

  private static AiException badResponse() {
    return new AiException(
        AiErrorCode.AI_BAD_RESPONSE, "AI returned an invalid structured response.");
  }
}
