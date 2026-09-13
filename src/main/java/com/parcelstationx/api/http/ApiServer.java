package com.parcelstationx.api.http;

import com.parcelstationx.api.auth.SessionManager;
import com.parcelstationx.api.dto.*;
import com.parcelstationx.api.error.BadRequestException;
import com.parcelstationx.api.error.HttpErrorException;
import com.parcelstationx.api.json.JsonCodec;
import com.parcelstationx.dao.ParcelDao;
import com.parcelstationx.dao.ParcelRelocationDao;
import com.parcelstationx.model.UserRole;
import com.parcelstationx.service.*;
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
    this(address, authentication, parcels, sessions, null, null, null);
  }

  public ApiServer(
      InetSocketAddress address,
      AuthenticationService authentication,
      ParcelDao parcels,
      SessionManager sessions,
      WarehouseLayoutService warehouse,
      RelocationService relocationService,
      ParcelRelocationDao relocations)
      throws IOException {
    JsonCodec json = new JsonCodec();
    Router router = new Router(json, sessions);
    registerRoutes(
        router, json, authentication, parcels, sessions, warehouse, relocationService, relocations);
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
      SessionManager sessions,
      WarehouseLayoutService warehouse,
      RelocationService relocationService,
      ParcelRelocationDao relocations) {
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
    router.add(new Route("GET", "/api/auth/me", true, null, c -> UserDto.from(c.user())));
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
            context ->
                parcels
                    .findById(parseId(context.pathParameter("id")))
                    .map(ParcelDto::from)
                    .orElseThrow(() -> new HttpErrorException(404, "快件不存在。", "PARCEL_NOT_FOUND"))));
    router.add(
        new Route("GET", "/api/admin/ping", true, UserRole.ADMIN, c -> Map.of("status", "UP")));

    if (warehouse != null && relocationService != null && relocations != null) {
      router.add(
          new Route(
              "GET",
              "/api/dashboard/summary",
              true,
              null,
              c -> DashboardDto.from(warehouse.snapshot())));
      router.add(
          new Route(
              "GET", "/api/warehouse", true, null, c -> WarehouseDto.from(warehouse.snapshot())));
      router.add(
          new Route(
              "POST",
              "/api/parcels/{id}/relocate",
              true,
              null,
              context -> {
                RelocateRequest request = json.read(context.body(), RelocateRequest.class);
                if (request.targetSlotId() == null || request.expectedVersion() == null) {
                  throw new BadRequestException(
                      "targetSlotId 和 expectedVersion 必填。", "MISSING_FIELD");
                }
                return RelocateResponse.from(
                    relocationService.relocate(
                        parseId(context.pathParameter("id")),
                        request.targetSlotId(),
                        request.expectedVersion(),
                        request.reason(),
                        context.user().id()));
              }));
      router.add(
          new Route(
              "GET",
              "/api/parcels/{id}/relocations",
              true,
              null,
              context -> relocations.findByParcelId(parseId(context.pathParameter("id")))));
    }
  }

  private static long parseId(String value) {
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException exception) {
      throw new BadRequestException("ID 格式错误。", "INVALID_ID");
    }
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
