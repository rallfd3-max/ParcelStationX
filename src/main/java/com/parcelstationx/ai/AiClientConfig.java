package com.parcelstationx.ai;

import com.parcelstationx.exception.ConfigurationException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;

public record AiClientConfig(
    boolean enabled,
    String provider,
    String portalUrl,
    String baseUrl,
    String apiKey,
    String model,
    String chatPath,
    Duration timeout,
    int maxOutputTokens) {

  public static final String DEFAULT_PROVIDER = "maimaiya";
  public static final String DEFAULT_PORTAL_URL = "https://maimaiya.click/profile";

  public static AiClientConfig load(Properties properties, Map<String, String> environment) {
    boolean enabled =
        Boolean.parseBoolean(
            value(environment, properties, "PARCEL_AI_ENABLED", "ai.enabled", "false"));
    String provider =
        value(environment, properties, "PARCEL_AI_PROVIDER", "ai.provider", DEFAULT_PROVIDER);
    String portal =
        value(environment, properties, "PARCEL_AI_PORTAL_URL", "ai.portal-url", DEFAULT_PORTAL_URL);
    String base = value(environment, properties, "PARCEL_AI_BASE_URL", "ai.base-url", "");
    String key = value(environment, properties, "PARCEL_AI_API_KEY", "ai.api-key", "");
    String model = value(environment, properties, "PARCEL_AI_MODEL", "ai.model", "");
    String path =
        value(
            environment, properties, "PARCEL_AI_CHAT_PATH", "ai.chat-path", "/v1/chat/completions");
    int timeout =
        positiveInt(
            value(environment, properties, "PARCEL_AI_TIMEOUT_SECONDS", "ai.timeout-seconds", "15"),
            "AI timeout");
    int maxTokens =
        positiveInt(
            value(
                environment,
                properties,
                "PARCEL_AI_MAX_OUTPUT_TOKENS",
                "ai.max-output-tokens",
                "1200"),
            "AI max output tokens");
    if (maxTokens > 16_384)
      throw new ConfigurationException("AI max output tokens must not exceed 16384.");
    if (enabled && (blank(base) || blank(key) || blank(model))) {
      throw new ConfigurationException(
          "AI is enabled but MaiMaiYa base URL, API key, or model is missing.");
    }
    if (!blank(base) && base.equalsIgnoreCase(portal)) {
      throw new ConfigurationException(
          "MaiMaiYa portal URL cannot be used as the API base URL unless explicitly configured by the provider.");
    }
    return new AiClientConfig(
        enabled, provider, portal, base, key, model, path, Duration.ofSeconds(timeout), maxTokens);
  }

  public URI chatEndpoint() {
    if (blank(baseUrl))
      throw new AiException(AiErrorCode.AI_DISABLED, "AI service is not configured.");
    String base = baseUrl.replaceAll("/+$", "");
    String path = chatPath.startsWith("/") ? chatPath : "/" + chatPath;
    if (base.endsWith("/v1") && path.startsWith("/v1/")) path = path.substring(3);
    try {
      return URI.create(base + path);
    } catch (IllegalArgumentException exception) {
      throw new ConfigurationException("AI base URL or chat path is invalid.", exception);
    }
  }

  public AiStatus status() {
    return new AiStatus(
        enabled, enabled && !blank(baseUrl) && !blank(apiKey) && !blank(model), provider, model);
  }

  private static String value(
      Map<String, String> environment,
      Properties properties,
      String envKey,
      String propertyKey,
      String fallback) {
    String envValue = environment.get(envKey);
    String value = blank(envValue) ? properties.getProperty(propertyKey) : envValue;
    return blank(value) ? fallback : value.trim();
  }

  private static int positiveInt(String value, String label) {
    try {
      int parsed = Integer.parseInt(value);
      if (parsed <= 0) throw new NumberFormatException();
      return parsed;
    } catch (NumberFormatException exception) {
      throw new ConfigurationException(label + " must be a positive integer.");
    }
  }

  private static boolean blank(String value) {
    return value == null || value.isBlank();
  }

  public record AiStatus(boolean enabled, boolean configured, String provider, String model) {}
}
