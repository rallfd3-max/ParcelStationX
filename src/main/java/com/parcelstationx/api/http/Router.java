package com.parcelstationx.api.http;

import com.parcelstationx.api.auth.SessionManager;
import com.parcelstationx.api.error.HttpErrorException;
import com.parcelstationx.api.json.JsonCodec;
import com.parcelstationx.model.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class Router implements HttpHandler {
  private final List<Route> routes = new ArrayList<>();
  private final JsonCodec json;
  private final SessionManager sessions;

  public Router(JsonCodec json, SessionManager sessions) {
    this.json = json;
    this.sessions = sessions;
  }

  public void add(Route route) {
    routes.add(route);
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      dispatch(exchange);
    } catch (Exception exception) {
      writeError(exchange, exception);
    } finally {
      exchange.close();
    }
  }

  private void dispatch(HttpExchange exchange) throws Exception {
    String path = exchange.getRequestURI().getPath();
    boolean knownPath = false;
    for (Route route : routes) {
      var parameters = route.parameters(path);
      if (parameters.isEmpty()) {
        continue;
      }
      knownPath = true;
      if (!route.matchesMethod(exchange.getRequestMethod())) {
        continue;
      }
      String token = bearerToken(exchange);
      User user = sessions.findUser(token).orElse(null);
      if (route.authenticated() && user == null) {
        throw new HttpErrorException(401, "请先登录。", "UNAUTHORIZED");
      }
      if (route.requiredRole() != null && user.role() != route.requiredRole()) {
        throw new HttpErrorException(403, "没有权限执行此操作。", "FORBIDDEN");
      }
      Object result =
          route.handler().handle(new RequestContext(exchange, parameters.get(), user, token));
      write(exchange, 200, ApiResponse.success(result));
      return;
    }
    if (knownPath) {
      throw new HttpErrorException(405, "请求方法不受支持。", "METHOD_NOT_ALLOWED");
    }
    throw new HttpErrorException(404, "接口不存在。", "NOT_FOUND");
  }

  private String bearerToken(HttpExchange exchange) {
    String authorization = exchange.getRequestHeaders().getFirst("Authorization");
    if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
      return null;
    }
    return authorization.substring(7).trim();
  }

  private void writeError(HttpExchange exchange, Exception exception) throws IOException {
    if (exception instanceof HttpErrorException error) {
      write(exchange, error.status(), ApiResponse.failure(error.getMessage(), error.code()));
    } else if (exception instanceof com.parcelstationx.api.error.ApiException error) {
      write(exchange, error.status(), ApiResponse.failure(error.getMessage(), error.code()));
    } else if (exception instanceof com.parcelstationx.exception.BusinessException) {
      write(exchange, 400, ApiResponse.failure(exception.getMessage(), "BUSINESS_ERROR"));
    } else {
      write(exchange, 500, ApiResponse.failure("服务器内部错误。", "INTERNAL_ERROR"));
    }
  }

  private void write(HttpExchange exchange, int status, Object response) throws IOException {
    byte[] bytes = json.write(response).getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
    exchange.getResponseHeaders().set("Cache-Control", "no-store");
    exchange.sendResponseHeaders(status, bytes.length);
    try (OutputStream output = exchange.getResponseBody()) {
      output.write(bytes);
    }
  }
}
