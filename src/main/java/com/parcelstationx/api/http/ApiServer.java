package com.parcelstationx.api.http;

import com.parcelstationx.api.auth.SessionManager;
import com.parcelstationx.api.dto.LoginRequest;
import com.parcelstationx.api.dto.LoginResponse;
import com.parcelstationx.api.dto.ParcelDto;
import com.parcelstationx.api.dto.UserDto;
import com.parcelstationx.api.error.BadRequestException;
import com.parcelstationx.api.error.HttpErrorException;
import com.parcelstationx.api.json.JsonCodec;
import com.parcelstationx.dao.ParcelDao;
import com.parcelstationx.model.UserRole;
import com.parcelstationx.service.AuthenticationService;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ApiServer implements AutoCloseable {
  private final HttpServer server;
  private final ExecutorService executor;

  public ApiServer(
      InetSocketAddress address,
      AuthenticationService authentication,
      ParcelDao parcels,
      SessionManager sessions)
      throws IOException {
    JsonCodec json = new JsonCodec();
    Router router = new Router(json, sessions);
    registerRoutes(router, json, authentication, parcels, sessions);
    server = HttpServer.create(address, 0);
    executor =
        Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors()));
    server.setExecutor(executor);
    server.createContext("/", router);
  }

  private static void registerRoutes(
      Router router,
      JsonCodec json,
      AuthenticationService authentication,
      ParcelDao parcels,
      SessionManager sessions) {
    router.add(new Route("GET", "/api/health", false, null, context -> Map.of("status", "UP")));
    router.add(
        new Route(
            "POST",
            "/api/auth/login",
            false,
            null,
            context -> {
              LoginRequest request = json.read(context.body(), LoginRequest.class);
              char[] password =
                  request.password() == null ? null : request.password().toCharArray();
              try {
                var user = authentication.login(request.username(), password);
                return new LoginResponse(sessions.create(user), UserDto.from(user));
              } catch (com.parcelstationx.exception.BusinessException exception) {
                throw new HttpErrorException(401, "用户名或密码错误。", "INVALID_CREDENTIALS");
              }
            }));
    router.add(
        new Route("GET", "/api/auth/me", true, null, context -> UserDto.from(context.user())));
    router.add(
        new Route(
            "POST",
            "/api/auth/logout",
            true,
            null,
            context -> {
              sessions.invalidate(context.token());
              return Map.of("loggedOut", true);
            }));
    router.add(
        new Route(
            "GET",
            "/api/parcels",
            true,
            null,
            context -> parcels.findAll().stream().map(ParcelDto::from).toList()));
    router.add(
        new Route(
            "GET",
            "/api/parcels/{id}",
            true,
            null,
            context -> {
              long id;
              try {
                id = Long.parseLong(context.pathParameter("id"));
              } catch (NumberFormatException exception) {
                throw new BadRequestException("快件 ID 格式错误。", "INVALID_ID");
              }
              return parcels
                  .findById(id)
                  .map(ParcelDto::from)
                  .orElseThrow(() -> new HttpErrorException(404, "快件不存在。", "PARCEL_NOT_FOUND"));
            }));
    router.add(
        new Route(
            "GET", "/api/admin/ping", true, UserRole.ADMIN, context -> Map.of("status", "UP")));
  }

  public void start() {
    server.start();
  }

  public int port() {
    return server.getAddress().getPort();
  }

  @Override
  public void close() {
    server.stop(0);
    executor.shutdownNow();
  }
}
