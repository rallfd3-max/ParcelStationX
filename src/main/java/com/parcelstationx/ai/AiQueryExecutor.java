package com.parcelstationx.ai;

import com.parcelstationx.model.ExceptionRecord;
import com.parcelstationx.model.Parcel;
import com.parcelstationx.model.ParcelStatus;
import com.parcelstationx.service.DashboardAnalyticsService;
import com.parcelstationx.service.ExceptionService;
import com.parcelstationx.service.WarehouseLayoutService;
import com.parcelstationx.service.WarehouseSnapshot;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public final class AiQueryExecutor {
  private final Supplier<WarehouseSnapshot> warehouse;
  private final Supplier<List<ExceptionRecord>> openExceptions;
  private final Supplier<DashboardAnalyticsService.AiContext> today;

  public AiQueryExecutor(
      WarehouseLayoutService warehouse,
      ExceptionService exceptions,
      DashboardAnalyticsService analytics) {
    this(warehouse::snapshot, exceptions::findOpen, analytics::aiContext);
  }

  AiQueryExecutor(
      Supplier<WarehouseSnapshot> warehouse,
      Supplier<List<ExceptionRecord>> openExceptions,
      Supplier<DashboardAnalyticsService.AiContext> today) {
    this.warehouse = warehouse;
    this.openExceptions = openExceptions;
    this.today = today;
  }

  public QueryResponse execute(AiIntentRequest request) {
    return switch (request.intent()) {
      case PARCEL_SEARCH, PARCEL_LOCATE -> parcelSearch(request);
      case OVERDUE_PARCELS -> overdue(request);
      case SHELF_AVAILABILITY -> availability(request);
      case SHELF_UTILIZATION -> utilization(request);
      case UNRESOLVED_EXCEPTIONS -> unresolved(request);
      case TODAY_SUMMARY -> summary(request);
      case UNSUPPORTED -> new QueryResponse("该请求不在只读白名单能力范围内。", List.of(), Action.none());
    };
  }

  private QueryResponse parcelSearch(AiIntentRequest request) {
    var filters = request.filters();
    List<ResultItem> results =
        warehouse.get().parcels().stream()
            .filter(p -> contains(p.trackingNo(), filters.trackingNo()))
            .filter(p -> contains(p.courierCompany(), filters.courier()))
            .limit(filters.limit())
            .map(this::parcelResult)
            .toList();
    Action action =
        request.intent() == AiIntent.PARCEL_LOCATE && results.size() == 1
            ? new Action(ActionType.FOCUS_PARCEL, results.get(0).parcelId())
            : Action.none();
    return new QueryResponse(
        results.isEmpty() ? "未找到符合条件的快件。" : "找到 " + results.size() + " 条快件。", results, action);
  }

  private QueryResponse overdue(AiIntentRequest request) {
    LocalDateTime now = LocalDateTime.now();
    List<ResultItem> results =
        warehouse.get().parcels().stream()
            .filter(p -> p.status() == ParcelStatus.IN_STOCK)
            .filter(p -> Duration.between(p.arrivedAt(), now).toDays() >= request.filters().days())
            .filter(p -> contains(p.courierCompany(), request.filters().courier()))
            .sorted(Comparator.comparing(Parcel::arrivedAt))
            .limit(request.filters().limit())
            .map(this::parcelResult)
            .toList();
    return new QueryResponse("找到 " + results.size() + " 条滞留快件。", results, Action.none());
  }

  private QueryResponse availability(AiIntentRequest request) {
    WarehouseSnapshot snapshot = warehouse.get();
    String zone = request.filters().zone();
    var shelfIds =
        snapshot.shelves().stream()
            .filter(
                s ->
                    zone == null
                        || s.zone()
                            .toUpperCase(Locale.ROOT)
                            .startsWith(zone.toUpperCase(Locale.ROOT)))
            .map(s -> s.id())
            .toList();
    long enabled =
        snapshot.slots().stream()
            .filter(s -> shelfIds.contains(s.shelfId()) && s.enabled())
            .count();
    long occupied =
        snapshot.parcels().stream()
            .filter(p -> p.slotId() != null)
            .filter(
                p ->
                    snapshot.slots().stream()
                        .anyMatch(s -> s.id().equals(p.slotId()) && shelfIds.contains(s.shelfId())))
            .count();
    return new QueryResponse(
        (zone == null ? "全部分区" : zone + "区") + "还有 " + Math.max(0, enabled - occupied) + " 个空仓位。",
        List.of(),
        Action.none());
  }

  private QueryResponse utilization(AiIntentRequest request) {
    var shelves =
        warehouse.get().shelves().stream()
            .sorted(
                Comparator.comparingDouble(
                    s -> -(s.capacity() == 0 ? 0 : (double) s.occupied() / s.capacity())))
            .limit(request.filters().limit())
            .map(
                s ->
                    new ResultItem(
                        null, s.shelfCode(), null, s.zone(), s.occupied() + "/" + s.capacity()))
            .toList();
    return new QueryResponse(
        shelves.isEmpty() ? "暂无货架数据。" : "当前最满货架是 " + shelves.get(0).trackingNo() + "。",
        shelves,
        Action.none());
  }

  private QueryResponse unresolved(AiIntentRequest request) {
    List<ResultItem> results =
        openExceptions.get().stream()
            .limit(request.filters().limit())
            .map(
                e ->
                    new ResultItem(
                        e.parcelId(), "异常 #" + e.id(), null, null, e.exceptionType().name()))
            .toList();
    return new QueryResponse("当前有 " + results.size() + " 条未处理异常。", results, Action.none());
  }

  private QueryResponse summary(AiIntentRequest request) {
    var value = today.get();
    return new QueryResponse(
        "今日入库 "
            + value.todayInbound()
            + " 件，出库 "
            + value.todayOutbound()
            + " 件，当前库存 "
            + value.inventory()
            + " 件，异常 "
            + value.exceptions()
            + " 件。",
        List.of(),
        Action.none());
  }

  private ResultItem parcelResult(Parcel parcel) {
    WarehouseSnapshot snapshot = warehouse.get();
    String slot =
        parcel.slotId() == null
            ? null
            : snapshot.slots().stream()
                .filter(s -> s.id().equals(parcel.slotId()))
                .map(s -> s.slotCode())
                .findFirst()
                .orElse(null);
    String shelf =
        parcel.shelfId() == null
            ? null
            : snapshot.shelves().stream()
                .filter(s -> s.id().equals(parcel.shelfId()))
                .map(s -> s.shelfCode())
                .findFirst()
                .orElse(null);
    return new ResultItem(parcel.id(), parcel.trackingNo(), slot, shelf, parcel.status().name());
  }

  private static boolean contains(String value, String filter) {
    return filter == null
        || value.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
  }

  public enum ActionType {
    FOCUS_PARCEL,
    OPEN_PARCEL_DETAIL,
    NONE
  }

  public record Action(ActionType type, Long parcelId) {
    static Action none() {
      return new Action(ActionType.NONE, null);
    }
  }

  public record ResultItem(
      Long parcelId, String trackingNo, String slotCode, String shelfCode, String status) {}

  public record QueryResponse(String answer, List<ResultItem> results, Action action) {}
}
