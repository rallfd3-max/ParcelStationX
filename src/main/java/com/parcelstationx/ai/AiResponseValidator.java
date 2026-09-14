package com.parcelstationx.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

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

  public String requireText(JsonNode object, String field, int maxLength) {
    JsonNode value = object.get(field);
    if (value == null || !value.isTextual() || value.asText().isBlank()) throw badResponse();
    String text = value.asText().trim();
    if (text.length() > maxLength) throw badResponse();
    return text;
  }

  public List<String> requireTextList(JsonNode object, String field, int maxItems, int maxLength) {
    JsonNode value = object.get(field);
    if (value == null || !value.isArray() || value.size() > maxItems) throw badResponse();
    List<String> result = new ArrayList<>();
    for (JsonNode item : value) {
      if (!item.isTextual() || item.asText().isBlank() || item.asText().length() > maxLength) {
        throw badResponse();
      }
      result.add(item.asText().trim());
    }
    return List.copyOf(result);
  }

  private static AiException badResponse() {
    return new AiException(
        AiErrorCode.AI_BAD_RESPONSE, "AI returned an invalid structured response.");
  }
}
