package com.parcelstationx.api.http;

import com.parcelstationx.api.error.BadRequestException;
import com.parcelstationx.model.User;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class RequestContext {
  private static final int MAX_BODY_BYTES = 1_048_576;
  private final HttpExchange exchange;
  private final Map<String, String> pathParameters;
  private final User user;
  private final String token;

  RequestContext(
      HttpExchange exchange, Map<String, String> pathParameters, User user, String token) {
    this.exchange = exchange;
    this.pathParameters = Map.copyOf(pathParameters);
    this.user = user;
    this.token = token;
  }

  public String body() throws IOException {
    byte[] bytes = exchange.getRequestBody().readNBytes(MAX_BODY_BYTES + 1);
    if (bytes.length > MAX_BODY_BYTES) {
      throw new BadRequestException("请求内容过大。", "REQUEST_TOO_LARGE");
    }
    return new String(bytes, StandardCharsets.UTF_8);
  }

  public String pathParameter(String name) {
    return pathParameters.get(name);
  }

  public User user() {
    return user;
  }

  public String token() {
    return token;
  }
}
