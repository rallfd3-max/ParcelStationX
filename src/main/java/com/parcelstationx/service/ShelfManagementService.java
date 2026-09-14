package com.parcelstationx.service;

import com.parcelstationx.dao.impl.OperationLogDaoImpl;
import com.parcelstationx.dao.impl.ShelfDaoImpl;
import com.parcelstationx.dao.impl.ShelfLayoutDaoImpl;
import com.parcelstationx.dao.impl.ShelfSlotDaoImpl;
import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.OperationLog;
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
  private final ShelfCodeGenerator codes;
  private final ShelfMutationValidator validator;
  private final Clock clock;

  public ShelfManagementService(
      TransactionRunner transactions,
      ShelfDaoImpl shelves,
      ShelfLayoutDaoImpl layouts,
      ShelfSlotDaoImpl slots,
      OperationLogDaoImpl logs) {
    this(
        transactions,
        shelves,
        layouts,
        slots,
        logs,
        new ShelfCodeGenerator(),
        new ShelfMutationValidator(),
        Clock.systemDefaultZone());
  }

  ShelfManagementService(
      TransactionRunner transactions,
      ShelfDaoImpl shelves,
      ShelfLayoutDaoImpl layouts,
      ShelfSlotDaoImpl slots,
      OperationLogDaoImpl logs,
      ShelfCodeGenerator codes,
      ShelfMutationValidator validator,
      Clock clock) {
    this.transactions = transactions;
    this.shelves = shelves;
    this.layouts = layouts;
    this.slots = slots;
    this.logs = logs;
    this.codes = codes;
    this.validator = validator;
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
        valid.depth());
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

    double nextX =
        currentLayouts.stream()
                .mapToDouble(layout -> layout.positionX() + layout.width() / 2.0)
                .max()
                .orElse(-request.width() / 2.0)
            + SHELF_GAP
            + request.width() / 2.0;
    LocalDateTime now = LocalDateTime.now(clock);
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
      ShelfLayout layout =
          new ShelfLayout(
              null,
              nextX + index * (request.width() + SHELF_GAP),
              0,
              0,
              0,
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
