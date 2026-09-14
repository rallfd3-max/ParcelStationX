package com.parcelstationx.ai;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parcelstationx.exception.ConfigurationException;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class AiClientConfigTest {
  @Test
  void defaultsToDisabledMaiMaiYaWithoutGuessingAnEndpoint() {
    AiClientConfig config = AiClientConfig.load(new Properties(), Map.of());
    assertFalse(config.enabled());
    assertEquals("maimaiya", config.provider());
    assertEquals("", config.baseUrl());
    assertFalse(config.status().configured());
  }

  @Test
  void enabledConfigurationIsCompleteAndNeverSerializesTheKeyInStatus() throws Exception {
    String secret = "test-secret-value";
    AiClientConfig config =
        AiClientConfig.load(
            new Properties(),
            Map.of(
                "PARCEL_AI_ENABLED", "true",
                "PARCEL_AI_BASE_URL", "https://relay.example/v1",
                "PARCEL_AI_API_KEY", secret,
                "PARCEL_AI_MODEL", "configured-model"));
    assertEquals("https://relay.example/v1/chat/completions", config.chatEndpoint().toString());
    String statusJson = new ObjectMapper().writeValueAsString(config.status());
    assertFalse(statusJson.contains(secret));
    assertTrue(config.status().configured());
  }

  @Test
  void rejectsPortalAsGuessedApiEndpointAndIncompleteEnabledConfig() {
    assertThrows(
        ConfigurationException.class,
        () ->
            AiClientConfig.load(
                new Properties(),
                Map.of(
                    "PARCEL_AI_ENABLED", "true",
                    "PARCEL_AI_BASE_URL", AiClientConfig.DEFAULT_PORTAL_URL,
                    "PARCEL_AI_API_KEY", "secret",
                    "PARCEL_AI_MODEL", "model")));
    assertThrows(
        ConfigurationException.class,
        () -> AiClientConfig.load(new Properties(), Map.of("PARCEL_AI_ENABLED", "true")));
  }
}
