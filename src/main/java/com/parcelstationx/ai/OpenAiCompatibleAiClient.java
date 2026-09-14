package com.parcelstationx.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public final class OpenAiCompatibleAiClient implements AiClient {
  private final AiClientConfig config;
  private final HttpClient client;
  private final ObjectMapper json;

  public OpenAiCompatibleAiClient(AiClientConfig config) {
    this(config, HttpClient.newBuilder().connectTimeout(config.timeout()).build());
  }

  public OpenAiCompatibleAiClient(AiClientConfig config, HttpClient client) {
    this.config = config;
    this.client = client;
    this.json = new ObjectMapper();
  }

  @Override
  public String generate(String systemPrompt, String userPrompt) {
    return generate(
        systemPrompt, userPrompt, new AiGenerationOptions(config.maxOutputTokens(), config.timeout()));
  }

  @Override
  public String generate(String systemPrompt, String userPrompt, AiGenerationOptions options) {
    if (!config.enabled())
      throw new AiException(AiErrorCode.AI_DISABLED, "AI service is disabled.");
    int maxTokens = Math.min(config.maxOutputTokens(), options.maxOutputTokens());
    Duration timeout = options.timeout().compareTo(config.timeout()) > 0 ? config.timeout() : options.timeout();
    Map<String, Object> body =
        Map.of(
            "model", config.model(),
            "max_tokens", maxTokens,
            "messages",
                List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)));
    try {
      HttpRequest request =
          HttpRequest.newBuilder(config.chatEndpoint())
              .timeout(timeout)
              .header("Authorization", "Bearer " + config.apiKey())
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
              .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      return parseResponse(response.statusCode(), response.body());
    } catch (HttpTimeoutException exception) {
      throw new AiException(AiErrorCode.AI_TIMEOUT, "AI service timed out.", exception);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new AiException(
          AiErrorCode.AI_UPSTREAM_ERROR, "AI request was interrupted.", exception);
    } catch (IOException exception) {
      throw new AiException(
          AiErrorCode.AI_UPSTREAM_ERROR, "AI service is temporarily unavailable.", exception);
    }
  }

  private String parseResponse(int status, String body) {
    if (status == 401 || status == 403)
      throw new AiException(
          AiErrorCode.AI_UNAUTHORIZED, "AI provider rejected the configured credentials.");
    if (status == 429)
      throw new AiException(AiErrorCode.AI_RATE_LIMITED, "AI provider rate limit was reached.");
    if (status < 200 || status >= 300)
      throw new AiException(
          AiErrorCode.AI_UPSTREAM_ERROR, "AI provider returned an upstream error.");
    try {
      JsonNode root = json.readTree(body);
      JsonNode content = root.path("choices").path(0).path("message").path("content");
      if (!content.isTextual() || content.asText().isBlank()) {
        throw new AiException(
            AiErrorCode.AI_BAD_RESPONSE, "AI provider returned an empty response.");
      }
      return content.asText();
    } catch (JsonProcessingException exception) {
      throw new AiException(
          AiErrorCode.AI_BAD_RESPONSE, "AI provider returned invalid JSON.", exception);
    }
  }
}
