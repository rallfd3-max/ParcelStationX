package com.parcelstationx.api.http;

import com.parcelstationx.api.auth.SessionManager;
import com.parcelstationx.api.dto.*;
import com.parcelstationx.api.error.BadRequestException;
import com.parcelstationx.api.error.HttpErrorException;
import com.parcelstationx.api.json.JsonCodec;
import com.parcelstationx.dao.ParcelDao;
import com.parcelstationx.dao.ParcelRelocationDao;
import com.parcelstationx.model.ExceptionType;
import com.parcelstationx.model.ParcelStatus;
import com.parcelstationx.model.ShelfLayout;
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
    this(address, authentication, parcels, sessions, null, null, null, null, null, null, null);
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
        router,
        json,
        authentication,
        parcels,
        sessions,
        warehouse,
        relocationService,
        relocations,
        null,
        null,
        null,
        null,
        null);
    server = HttpServer.create(address, 0);
    executor =
        Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors()));
    server.setExecutor(executor);
    server.createContext("/", router);
  }

  public ApiServer(
      InetSocketAddress address,
      AuthenticationService authentication,
      ParcelDao parcels,
      SessionManager sessions,
      WarehouseLayoutService warehouse,
      RelocationService relocationService,
      ParcelRelocationDao relocations,
      ParcelQueryService parcelQueries,
      ExceptionService exceptionService,
      UserService userService,
      ParcelService parcelService)
      throws IOException {
    this(address, authentication, parcels, sessions, warehouse, relocationService, relocations, parcelQueries, exceptionService, userService, parcelService, null);
  }

  public ApiServer(
      InetSocketAddress address, AuthenticationService authentication, ParcelDao parcels,
      SessionManager sessions, WarehouseLayoutService warehouse, RelocationService relocationService,
      ParcelRelocationDao relocations, ParcelQueryService parcelQueries, ExceptionService exceptionService,
      UserService userService, ParcelService parcelService, DashboardAnalyticsService dashboardAnalytics)
      throws IOException {
    JsonCodec json = new JsonCodec();
    Router router = new Router(json, sessions);
    registerRoutes(
        router,
        json,
        authentication,
        parcels,
        sessions,
        warehouse,
        relocationService,
        relocations,
        parcelQueries,
        exceptionService,
        userService,
        parcelService,
        dashboardAnalytics);
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
      ParcelRelocationDao relocations,
      ParcelQueryService parcelQueries,
      ExceptionService exceptionService,
      UserService userService,
      ParcelService parcelService,
      DashboardAnalyticsService dashboardAnalytics) {
    router.add(new Route("GET", "/api/health", false, null, context -> Map.of("status", "UP")));
    if (dashboardAnalytics != null) {
      router.add(new Route("GET", "/api/dashboard/trends", true, null, c -> dashboardAnalytics.trends()));
      router.add(new Route("GET", "/api/dashboard/distributions", true, null, c -> dashboardAnalytics.distributions()));
      router.add(new Route("GET", "/api/dashboard/activity", true, null, c -> dashboardAnalytics.activity()));
    }
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
    if (parcelQueries != null) {
      router.add(
          new Route(
              "GET",
              "/api/parcel-details",
              true,
              null,
              c -> parcelQueries.findAllDetails().stream().map(ParcelDetailsDto::from).toList()));
      router.add(
          new Route(
              "GET",
              "/api/parcels/{id}/details",
              true,
              null,
              c -> ParcelDetailsDto.from(parcelQueries.details(parseId(c.pathParameter("id"))))));
      router.add(
          new Route(
              "GET",
              "/api/parcels/{id}/events",
              true,
              null,
              c -> parcelQueries.details(parseId(c.pathParameter("id"))).events()));
    }
    if (exceptionService != null) {
      router.add(new Route("GET", "/api/exceptions", true, null, c -> exceptionService.findAll()));
      router.add(
          new Route(
              "POST",
              "/api/parcels/{id}/exception",
              true,
              null,
              c -> {
                CreateExceptionRequest request = json.read(c.body(), CreateExceptionRequest.class);
                return exceptionService.create(
                    parseId(c.pathParameter("id")),
                    enumValue(ExceptionType.class, request.exceptionType(), "异常类型无效。"),
                    requireText(request.description(), "异常描述必填。"),
                    c.user().id());
              }));
      router.add(
          new Route(
              "POST",
              "/api/exceptions/{id}/resolve",
              true,
              null,
              c -> {
                ResolveExceptionRequest request =
                    json.read(c.body(), ResolveExceptionRequest.class);
                return exceptionService.resolve(
                    parseId(c.pathParameter("id")),
                    enumValue(ParcelStatus.class, request.targetStatus(), "目标状态无效。"),
                    requireText(request.resolution(), "处理结果必填。"),
                    c.user().id());
              }));
    }
    if (parcelService != null) {
      router.add(
          new Route(
              "POST",
              "/api/parcels/inbound",
              true,
              null,
              c -> {
                InboundParcelRequest request = json.read(c.body(), InboundParcelRequest.class);
                return ParcelDto.from(
                    parcelService.inbound(
                        new InboundRequest(
                            request.trackingNo(),
                            requireText(request.courierCompany(), "快递公司必填。"),
                            request.customerMobile(),
                            null,
                            c.user().id(),
                            request.remark())));
              }));
      router.add(
          new Route(
              "POST",
              "/api/parcels/{id}/outbound",
              true,
              null,
              c -> {
                long id = parseId(c.pathParameter("id"));
                OutboundParcelRequest request = json.read(c.body(), OutboundParcelRequest.class);
                var current =
                    parcels
                        .findById(id)
                        .orElseThrow(
                            () -> new HttpErrorException(404, "快件不存在。", "PARCEL_NOT_FOUND"));
                if (!current.pickupCode().equals(request.pickupCode()))
                  throw new BadRequestException("取件码与快件不匹配。", "PARCEL_MISMATCH");
                return ParcelDto.from(parcelService.outbound(request.pickupCode(), c.user().id()));
              }));
    }
    if (userService != null && warehouse != null) {
      router.add(
          new Route(
              "GET",
              "/api/admin/users",
              true,
              UserRole.ADMIN,
              c -> userService.findAll().stream().map(UserDto::from).toList()));
      router.add(
          new Route(
              "POST",
              "/api/admin/users",
              true,
              UserRole.ADMIN,
              c -> {
                CreateUserRequest request = json.read(c.body(), CreateUserRequest.class);
                return UserDto.from(
                    userService.create(
                        request.username(),
                        request.password(),
                        request.displayName(),
                        enumValue(UserRole.class, request.role(), "角色无效。")));
              }));
      router.add(
          new Route(
              "PUT",
              "/api/admin/users/{id}/enabled",
              true,
              UserRole.ADMIN,
              c -> {
                SetEnabledRequest request = json.read(c.body(), SetEnabledRequest.class);
                if (request.enabled() == null)
                  throw new BadRequestException("enabled 必填。", "MISSING_FIELD");
                return UserDto.from(
                    userService.setEnabled(parseId(c.pathParameter("id")), request.enabled()));
              }));
      router.add(
          new Route(
              "GET",
              "/api/admin/warehouse",
              true,
              UserRole.ADMIN,
              c -> WarehouseDto.from(warehouse.snapshot())));
      router.add(
          new Route(
              "PUT",
              "/api/admin/layouts/{id}",
              true,
              UserRole.ADMIN,
              c -> {
                long id = parseId(c.pathParameter("id"));
                ShelfLayout request = json.read(c.body(), ShelfLayout.class);
                if (request.shelfId() != null && request.shelfId() != id)
                  throw new BadRequestException("路径与布局 shelfId 不一致。", "ID_MISMATCH");
                return warehouse.updateLayout(
                    new ShelfLayout(
                        id,
                        request.positionX(),
                        request.positionY(),
                        request.positionZ(),
                        request.rotationY(),
                        request.width(),
                        request.height(),
                        request.depth(),
                        request.columns(),
                        request.levels(),
                        null));
              }));
      router.add(
          new Route(
              "PUT",
              "/api/admin/slots/{id}/enabled",
              true,
              UserRole.ADMIN,
              c -> {
                SetSlotEnabledRequest request = json.read(c.body(), SetSlotEnabledRequest.class);
                if (request.enabled() == null)
                  throw new BadRequestException("enabled 必填。", "MISSING_FIELD");
                return warehouse.setSlotEnabled(parseId(c.pathParameter("id")), request.enabled());
              }));
    }
  }

  private static String requireText(String value, String message) {
    if (value == null || value.isBlank()) throw new BadRequestException(message, "MISSING_FIELD");
    return value.trim();
  }

  private static <E extends Enum<E>> E enumValue(Class<E> type, String value, String message) {
    try {
      return Enum.valueOf(type, value == null ? "" : value);
    } catch (IllegalArgumentException exception) {
      throw new BadRequestException(message, "INVALID_ENUM");
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
