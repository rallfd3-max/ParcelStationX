package com.parcelstationx.service;

import com.parcelstationx.dao.impl.OperationLogDaoImpl;
import com.parcelstationx.dao.impl.ParcelDaoImpl;
import com.parcelstationx.dao.impl.ShelfDaoImpl;
import com.parcelstationx.dao.impl.ShelfLayoutDaoImpl;
import com.parcelstationx.dao.impl.ShelfSlotDaoImpl;
import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.OperationLog;
import com.parcelstationx.model.ParcelStatus;
import com.parcelstationx.model.Shelf;
import com.parcelstationx.model.ShelfLayout;
import com.parcelstationx.model.ShelfSlot;
import com.parcelstationx.model.ShelfStatus;
import java.sql.Connection;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ShelfManagementService {
  private static final double SHELF_GAP = 0.6;

  private final TransactionRunner transactions;
  private final ShelfDaoImpl shelves;
  private final ShelfLayoutDaoImpl layouts;
  private final ShelfSlotDaoImpl slots;
  private final OperationLogDaoImpl logs;
  private final ParcelDaoImpl parcels;
  private final ShelfCodeGenerator codes;
  private final ShelfMutationValidator validator;
  private final ShelfAutoLayoutService autoLayout;
  private final Clock clock;

  public ShelfManagementService(
      TransactionRunner transactions,
      ShelfDaoImpl shelves,
      ShelfLayoutDaoImpl layouts,
      ShelfSlotDaoImpl slots,
      OperationLogDaoImpl logs) {
    this(transactions, shelves, layouts, slots, logs, null);
  }

  public ShelfManagementService(
      TransactionRunner transactions,
      ShelfDaoImpl shelves,
      ShelfLayoutDaoImpl layouts,
      ShelfSlotDaoImpl slots,
      OperationLogDaoImpl logs,
      ParcelDaoImpl parcels) {
    this(
        transactions,
        shelves,
        layouts,
        slots,
        logs,
        parcels,
        new ShelfCodeGenerator(),
        new ShelfMutationValidator(),
        new ShelfAutoLayoutService(),
        Clock.systemDefaultZone());
  }

  ShelfManagementService(
      TransactionRunner transactions,
      ShelfDaoImpl shelves,
      ShelfLayoutDaoImpl layouts,
      ShelfSlotDaoImpl slots,
      OperationLogDaoImpl logs,
      ParcelDaoImpl parcels,
      ShelfCodeGenerator codes,
      ShelfMutationValidator validator,
      ShelfAutoLayoutService autoLayout,
      Clock clock) {
    this.transactions = transactions;
    this.shelves = shelves;
    this.layouts = layouts;
    this.slots = slots;
    this.logs = logs;
    this.parcels = parcels;
    this.codes = codes;
    this.validator = validator;
    this.autoLayout = autoLayout;
    this.clock = clock;
  }

  public ShelfCreationResult preview(ShelfCreationRequest request) {
    ShelfCreationRequest valid = normalize(request);
    List<Shelf> existing = shelves.findAll();
    List<ShelfLayout> currentLayouts = layouts.findAll();
    return buildPreview(valid, existing, currentLayouts);
  }

  public ShelfCreationResult create(ShelfCreationRequest request, long operatorId) {
    ShelfCreationRequest valid = normalize(request);
    return transactions.run(
        connection -> {
          List<Shelf> existing = shelves.findAll(connection);
          List<ShelfLayout> currentLayouts = layouts.findAll(connection);
          ShelfCreationResult plan = buildPreview(valid, existing, currentLayouts);
          LocalDateTime now = LocalDateTime.now(clock);
          List<ShelfCreationItem> created = new ArrayList<>();
          for (ShelfCreationItem item : plan.shelves()) {
            Shelf planned = item.shelf();
            Shelf savedShelf =
                shelves.save(
                    connection,
                    new Shelf(
                        null,
                        planned.shelfCode(),
                        planned.zone(),
                        planned.capacity(),
                        0,
                        ShelfStatus.ACTIVE,
                        now));
            ShelfLayout plannedLayout = item.layout();
            ShelfLayout savedLayout =
                layouts.save(
                    connection,
                    new ShelfLayout(
                        savedShelf.id(),
                        plannedLayout.positionX(),
                        plannedLayout.positionY(),
                        plannedLayout.positionZ(),
                        plannedLayout.rotationY(),
                        plannedLayout.width(),
                        plannedLayout.height(),
                        plannedLayout.depth(),
                        plannedLayout.columns(),
                        plannedLayout.levels(),
                        now));
            createSlots(connection, savedShelf, valid.levels(), valid.columns(), now);
            created.add(new ShelfCreationItem(savedShelf, savedLayout, item.slotCount()));
          }
          String operation = valid.count() == 1 ? "SHELF_CREATE" : "SHELF_BATCH_CREATE";
          logs.save(
              connection,
              new OperationLog(
                  null,
                  operatorId,
                  operation,
                  "SHELF",
                  created.size() == 1 ? created.get(0).shelf().id() : null,
                  "创建 " + created.size() + " 个货架和 " + plan.slotCount() + " 个仓位",
                  now));
          return new ShelfCreationResult(created, created.size(), plan.slotCount(), false);
        });
  }

  public ShelfLayout move(long shelfId, ShelfLayout requested, long operatorId) {
    return transactions.run(
        connection -> {
          Shelf shelf = requireShelf(connection, shelfId);
          if (shelf.status() != ShelfStatus.ACTIVE) throw new BusinessException("停用货架不能移动。");
          ShelfLayout current = requireLayout(connection, shelfId);
          ShelfLayout candidate =
              new ShelfLayout(
                  shelfId,
                  requested.positionX(),
                  requested.positionY(),
                  requested.positionZ(),
                  requested.rotationY(),
                  current.width(),
                  current.height(),
                  current.depth(),
                  current.columns(),
                  current.levels(),
                  LocalDateTime.now(clock));
          List<ShelfLayout> others =
              layouts.findAll(connection).stream()
                  .filter(value -> !value.shelfId().equals(shelfId))
                  .toList();
          if (autoLayout.collides(candidate, others)) throw new BusinessException("目标位置与其他货架重叠。");
          layouts.save(connection, candidate);
          log(connection, operatorId, "SHELF_LAYOUT_UPDATE", shelfId, "移动货架 " + shelf.shelfCode());
          return candidate;
        });
  }

  public ShelfLayout resize(
      long shelfId,
      int levels,
      int columns,
      double width,
      double height,
      double depth,
      long operatorId) {
    validator.validate(
        new ShelfCreationRequest(null, "A", 1, levels, columns, width, height, depth));
    return transactions.run(
        connection -> {
          Shelf shelf = requireShelf(connection, shelfId);
          ShelfLayout current = requireLayout(connection, shelfId);
          List<ShelfSlot> currentSlots = slots.findByShelfId(connection, shelfId);
          Set<Long> occupiedSlots = activeSlotIds(connection);
          for (ShelfSlot slot : currentSlots) {
            if ((slot.levelIndex() > levels || slot.columnIndex() > columns)
                && occupiedSlots.contains(slot.id())) {
              throw new BusinessException("缩容范围包含占用中的仓位。");
            }
          }
          LocalDateTime now = LocalDateTime.now(clock);
          Set<String> grid = new HashSet<>();
          for (ShelfSlot slot : currentSlots) {
            grid.add(slot.levelIndex() + ":" + slot.columnIndex());
            boolean enabled = slot.levelIndex() <= levels && slot.columnIndex() <= columns;
            if (slot.enabled() != enabled) {
              slots.save(
                  connection,
                  new ShelfSlot(
                      slot.id(),
                      shelfId,
                      slot.slotCode(),
                      slot.levelIndex(),
                      slot.columnIndex(),
                      enabled,
                      slot.createdAt()));
            }
          }
          for (int level = 1; level <= levels; level++) {
            for (int column = 1; column <= columns; column++) {
              if (!grid.contains(level + ":" + column)) {
                slots.save(
                    connection,
                    new ShelfSlot(
                        null,
                        shelfId,
                        codes.slotCode(shelf.shelfCode(), level, column),
                        level,
                        column,
                        true,
                        now));
              }
            }
          }
          Shelf updatedShelf =
              new Shelf(
                  shelf.id(),
                  shelf.shelfCode(),
                  shelf.zone(),
                  levels * columns,
                  shelf.occupied(),
                  shelf.status(),
                  shelf.createdAt());
          shelves.save(connection, updatedShelf);
          ShelfLayout updated =
              new ShelfLayout(
                  shelfId,
                  current.positionX(),
                  current.positionY(),
                  current.positionZ(),
                  current.rotationY(),
                  width,
                  height,
                  depth,
                  columns,
                  levels,
                  now);
          List<ShelfLayout> others =
              layouts.findAll(connection).stream()
                  .filter(value -> !value.shelfId().equals(shelfId))
                  .toList();
          if (autoLayout.collides(updated, others)) throw new BusinessException("调整尺寸后将与其他货架重叠。");
          layouts.save(connection, updated);
          log(connection, operatorId, "SHELF_RESIZE", shelfId, "调整货架 " + shelf.shelfCode());
          return updated;
        });
  }

  public Shelf setEnabled(long shelfId, boolean enabled, long operatorId) {
    return transactions.run(
        connection -> {
          Shelf shelf = requireShelf(connection, shelfId);
          if (!enabled && hasActiveParcel(connection, shelfId))
            throw new BusinessException("当前货架仍有快件，请先移库或出库。");
          Shelf updated =
              new Shelf(
                  shelf.id(),
                  shelf.shelfCode(),
                  shelf.zone(),
                  shelf.capacity(),
                  shelf.occupied(),
                  enabled ? ShelfStatus.ACTIVE : ShelfStatus.DISABLED,
                  shelf.createdAt());
          shelves.save(connection, updated);
          log(connection, operatorId, "SHELF_STATUS_UPDATE", shelfId, enabled ? "启用货架" : "停用货架");
          return updated;
        });
  }

  private ShelfCreationRequest normalize(ShelfCreationRequest request) {
    ShelfCreationRequest valid = validator.validate(request);
    String zone = codes.normalizeZone(valid.zone());
    String shelfCode = codes.normalizeExplicitCode(valid.shelfCode());
    if (shelfCode != null && !shelfCode.startsWith(zone + "-"))
      throw new BusinessException("货架编号必须与区域一致。");
    return new ShelfCreationRequest(
        shelfCode,
        zone,
        valid.count(),
        valid.levels(),
        valid.columns(),
        valid.width(),
        valid.height(),
        valid.depth(),
        valid.layoutMode(),
        valid.maxShelvesPerRow(),
        valid.shelfGap(),
        valid.aisleGap());
  }

  private ShelfCreationResult buildPreview(
      ShelfCreationRequest request, List<Shelf> existing, List<ShelfLayout> currentLayouts) {
    Set<String> usedCodes = new HashSet<>();
    existing.forEach(shelf -> usedCodes.add(shelf.shelfCode().toUpperCase()));
    List<String> shelfCodes;
    if (request.shelfCode() != null) {
      if (usedCodes.contains(request.shelfCode())) throw new BusinessException("货架编号已存在。");
      shelfCodes = List.of(request.shelfCode());
    } else {
      shelfCodes = codes.generate(request.zone(), request.count(), existing);
    }

    LocalDateTime now = LocalDateTime.now(clock);
    List<ShelfLayout> plannedLayouts =
        autoLayout.plan(
            currentLayouts,
            new ShelfAutoLayoutRequest(
                request.count(),
                request.width(),
                request.height(),
                request.depth(),
                request.layoutMode(),
                request.maxShelvesPerRow(),
                request.shelfGap(),
                request.aisleGap()),
            now);
    List<ShelfCreationItem> items = new ArrayList<>();
    for (int index = 0; index < shelfCodes.size(); index++) {
      String code = shelfCodes.get(index);
      Shelf shelf =
          new Shelf(
              null,
              code,
              request.zone(),
              request.levels() * request.columns(),
              0,
              ShelfStatus.ACTIVE,
              now);
      ShelfLayout coordinates = plannedLayouts.get(index);
      ShelfLayout layout =
          new ShelfLayout(
              null,
              coordinates.positionX(),
              0,
              coordinates.positionZ(),
              coordinates.rotationY(),
              request.width(),
              request.height(),
              request.depth(),
              request.columns(),
              request.levels(),
              now);
      items.add(new ShelfCreationItem(shelf, layout, request.levels() * request.columns()));
    }
    return new ShelfCreationResult(
        items, items.size(), items.size() * request.levels() * request.columns(), true);
  }

  private Shelf requireShelf(Connection connection, long shelfId) {
    return shelves.findById(connection, shelfId).orElseThrow(() -> new BusinessException("货架不存在。"));
  }

  private ShelfLayout requireLayout(Connection connection, long shelfId) {
    return layouts
        .findById(connection, shelfId)
        .orElseThrow(() -> new BusinessException("货架布局不存在。"));
  }

  private boolean hasActiveParcel(Connection connection, long shelfId) {
    return parcels != null
        && parcels.findAll(connection).stream()
            .anyMatch(
                parcel ->
                    shelfId == (parcel.shelfId() == null ? -1 : parcel.shelfId())
                        && isActive(parcel.status()));
  }

  private Set<Long> activeSlotIds(Connection connection) {
    if (parcels == null) return Set.of();
    Set<Long> result = new HashSet<>();
    parcels.findAll(connection).stream()
        .filter(parcel -> parcel.slotId() != null && isActive(parcel.status()))
        .forEach(parcel -> result.add(parcel.slotId()));
    return result;
  }

  private boolean isActive(ParcelStatus status) {
    return status == ParcelStatus.IN_STOCK || status == ParcelStatus.EXCEPTION;
  }

  private void log(
      Connection connection, long operatorId, String operation, long shelfId, String description) {
    logs.save(
        connection,
        new OperationLog(
            null, operatorId, operation, "SHELF", shelfId, description, LocalDateTime.now(clock)));
  }

  private void createSlots(
      Connection connection, Shelf shelf, int levels, int columns, LocalDateTime now) {
    for (int level = 1; level <= levels; level++) {
      for (int column = 1; column <= columns; column++) {
        slots.save(
            connection,
            new ShelfSlot(
                null,
                shelf.id(),
                codes.slotCode(shelf.shelfCode(), level, column),
                level,
                column,
                true,
                now));
      }
    }
  }
}
