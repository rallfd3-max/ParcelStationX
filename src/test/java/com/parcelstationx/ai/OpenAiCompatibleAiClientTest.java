package com.parcelstationx.ai;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class OpenAiCompatibleAiClientTest {
  private HttpServer server;

  @AfterEach
  void closeServer() {
    if (server != null) server.stop(0);
  }

  @Test
  void sendsCompatibleRequestAndParsesContentWithoutLeakingKey() throws Exception {
    server = server(200, "{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}", 0);
    String secret = "unit-test-secret";
    OpenAiCompatibleAiClient client = new OpenAiCompatibleAiClient(config(secret, 2));
    assertEquals("ok", client.generate("system", "user"));
  }

  @Test
  void mapsProviderAndResponseFailuresToStableCodes() throws Exception {
    assertFailure(401, "{}", AiErrorCode.AI_UNAUTHORIZED);
    assertFailure(403, "{}", AiErrorCode.AI_UNAUTHORIZED);
    assertFailure(429, "{}", AiErrorCode.AI_RATE_LIMITED);
    assertFailure(500, "private upstream body", AiErrorCode.AI_UPSTREAM_ERROR);
    assertFailure(200, "not-json", AiErrorCode.AI_BAD_RESPONSE);
    assertFailure(
        200, "{\"choices\":[{\"message\":{\"content\":\"\"}}]}", AiErrorCode.AI_BAD_RESPONSE);
  }

  @Test
  void timesOutAndDisabledClientDoesNotCallNetwork() throws Exception {
    server = server(200, "{\"choices\":[{\"message\":{\"content\":\"late\"}}]}", 1500);
    AiException timeout =
        assertThrows(
            AiException.class,
            () -> new OpenAiCompatibleAiClient(config("secret", 1)).generate("s", "u"));
    assertEquals(AiErrorCode.AI_TIMEOUT, timeout.code());
    AiClientConfig disabled =
        new AiClientConfig(
            false,
            "maimaiya",
            AiClientConfig.DEFAULT_PORTAL_URL,
            "",
            "",
            "",
            "/v1/chat/completions",
            Duration.ofSeconds(1),
            100);
    AiException error =
        assertThrows(
            AiException.class, () -> new OpenAiCompatibleAiClient(disabled).generate("s", "u"));
    assertEquals(AiErrorCode.AI_DISABLED, error.code());
  }

  @Test
  void structuredValidatorAcceptsObjectAndRejectsOtherOutput() {
    AiResponseValidator validator = new AiResponseValidator();
    assertEquals(
        "ok", validator.requireObject("```json\n{\"value\":\"ok\"}\n```").get("value").asText());
    assertEquals(
        AiErrorCode.AI_BAD_RESPONSE,
        assertThrows(AiException.class, () -> validator.requireObject("[]")).code());
  }

  private void assertFailure(int status, String body, AiErrorCode expected) throws Exception {
    if (server != null) server.stop(0);
    server = server(status, body, 0);
    AiException error =
        assertThrows(
            AiException.class,
            () -> new OpenAiCompatibleAiClient(config("do-not-leak", 2)).generate("s", "u"));
    assertEquals(expected, error.code());
    assertFalse(error.getMessage().contains("do-not-leak"));
    assertFalse(error.getMessage().contains(body));
  }

  private AiClientConfig config(String key, int timeoutSeconds) {
    return new AiClientConfig(
        true,
        "maimaiya",
        AiClientConfig.DEFAULT_PORTAL_URL,
        "http://127.0.0.1:" + server.getAddress().getPort(),
        key,
        "test-model",
        "/v1/chat/completions",
        Duration.ofSeconds(timeoutSeconds),
        1200);
  }

  private HttpServer server(int status, String body, long delayMillis) throws Exception {
    HttpServer result = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    result.createContext(
        "/v1/chat/completions",
        exchange -> {
          try {
            if (delayMillis > 0) Thread.sleep(delayMillis);
            byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
          } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
          } finally {
            exchange.close();
          }
        });
    result.start();
    return result;
  }
}
