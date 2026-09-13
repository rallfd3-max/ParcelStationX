package com.parcelstationx.service;

import com.parcelstationx.api.error.ConflictException;
import com.parcelstationx.api.error.UnprocessableException;
import com.parcelstationx.dao.impl.OperationLogDaoImpl;
import com.parcelstationx.dao.impl.ParcelDaoImpl;
import com.parcelstationx.dao.impl.ParcelEventDaoImpl;
import com.parcelstationx.dao.impl.ParcelRelocationDaoImpl;
import com.parcelstationx.dao.impl.ShelfDaoImpl;
import com.parcelstationx.dao.impl.ShelfSlotDaoImpl;
import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.OperationLog;
import com.parcelstationx.model.Parcel;
import com.parcelstationx.model.ParcelEvent;
import com.parcelstationx.model.ParcelRelocation;
import com.parcelstationx.model.ParcelStatus;
import com.parcelstationx.model.Shelf;
import java.time.Clock;
import java.time.LocalDateTime;

public final class RelocationService {
  private final TransactionRunner transactions;
  private final ParcelDaoImpl parcels;
  private final ShelfSlotDaoImpl slots;
  private final ShelfDaoImpl shelves;
  private final ParcelRelocationDaoImpl relocations;
  private final ParcelEventDaoImpl events;
  private final OperationLogDaoImpl logs;
  private final Clock clock;

  public RelocationService(
      TransactionRunner transactions,
      ParcelDaoImpl parcels,
      ShelfSlotDaoImpl slots,
      ShelfDaoImpl shelves,
      ParcelRelocationDaoImpl relocations,
      ParcelEventDaoImpl events,
      OperationLogDaoImpl logs) {
    this(
        transactions,
        parcels,
        slots,
        shelves,
        relocations,
        events,
        logs,
        Clock.systemDefaultZone());
  }

  RelocationService(
      TransactionRunner transactions,
      ParcelDaoImpl parcels,
      ShelfSlotDaoImpl slots,
      ShelfDaoImpl shelves,
      ParcelRelocationDaoImpl relocations,
      ParcelEventDaoImpl events,
      OperationLogDaoImpl logs,
      Clock clock) {
    this.transactions = transactions;
    this.parcels = parcels;
    this.slots = slots;
    this.shelves = shelves;
    this.relocations = relocations;
    this.events = events;
    this.logs = logs;
    this.clock = clock;
  }

  public RelocationResult relocate(
      long parcelId, long targetSlotId, long expectedVersion, String reason, long operatorId) {
    String normalizedReason =
        reason == null || reason.isBlank() ? "manual relocation" : reason.trim();
    if (normalizedReason.length() > 255) {
      throw new BusinessException("移动原因不能超过 255 个字符。");
    }
    return transactions.run(
        connection -> {
          Parcel parcel =
              parcels
                  .findByIdForUpdate(connection, parcelId)
                  .orElseThrow(() -> new BusinessException("快件不存在。"));
          if (parcel.status() != ParcelStatus.IN_STOCK) {
            throw new UnprocessableException("当前快件状态不允许移动。", "PARCEL_STATE_INVALID");
          }
          if (parcel.version() != expectedVersion) {
            throw new ConflictException("快件版本已变化，请刷新后重试。", "PARCEL_VERSION_CONFLICT");
          }
          var target = slots.findByIdForUpdate(connection, targetSlotId);
          if (!target.enabled()) {
            throw new UnprocessableException("目标仓位已停用。", "SLOT_DISABLED");
          }
          if (targetSlotId == (parcel.slotId() == null ? -1L : parcel.slotId())) {
            throw new BusinessException("快件已在目标仓位。");
          }
          if (parcels.findBySlotId(connection, targetSlotId).isPresent()) {
            throw new ConflictException("目标仓位已被占用。", "SLOT_OCCUPIED");
          }

          Shelf targetShelf =
              shelves
                  .findById(connection, target.shelfId())
                  .orElseThrow(() -> new BusinessException("目标货架不存在。"));
          if (targetShelf.occupied() >= targetShelf.capacity()) {
            throw new ConflictException("目标货架已满。", "SHELF_FULL");
          }
          Shelf oldShelf = null;
          if (parcel.slotId() != null && parcel.shelfId() != null) {
            oldShelf = shelves.findById(connection, parcel.shelfId()).orElse(null);
          }
          if (!parcels.assignSlot(
              connection, parcel.id(), target.shelfId(), targetSlotId, expectedVersion)) {
            throw new ConflictException("快件版本已变化，请刷新后重试。", "PARCEL_VERSION_CONFLICT");
          }
          if (oldShelf != null && !oldShelf.id().equals(targetShelf.id())) {
            shelves.save(connection, withOccupied(oldShelf, Math.max(0, oldShelf.occupied() - 1)));
          }
          if (oldShelf == null || !oldShelf.id().equals(targetShelf.id())) {
            shelves.save(connection, withOccupied(targetShelf, targetShelf.occupied() + 1));
          }
          LocalDateTime now = LocalDateTime.now(clock);
          ParcelRelocation relocation =
              relocations.save(
                  connection,
                  new ParcelRelocation(
                      null,
                      parcel.id(),
                      parcel.slotId(),
                      targetSlotId,
                      operatorId,
                      normalizedReason,
                      now));
          events.save(
              connection,
              new ParcelEvent(
                  null,
                  parcel.id(),
                  "RELOCATED",
                  ParcelStatus.IN_STOCK,
                  ParcelStatus.IN_STOCK,
                  operatorId,
                  "快件移动到仓位 " + target.slotCode(),
                  now));
          logs.save(
              connection,
              new OperationLog(
                  null,
                  operatorId,
                  "RELOCATE",
                  "PARCEL",
                  parcel.id(),
                  "快件 " + parcel.trackingNo() + " 移动到 " + target.slotCode(),
                  now));
          Parcel updated =
              new Parcel(
                  parcel.id(),
                  parcel.trackingNo(),
                  parcel.courierCompany(),
                  parcel.customerId(),
                  target.shelfId(),
                  parcel.pickupCode(),
                  parcel.status(),
                  parcel.arrivedAt(),
                  parcel.pickedUpAt(),
                  parcel.operatorId(),
                  parcel.remark(),
                  parcel.createdAt(),
                  now,
                  targetSlotId,
                  expectedVersion + 1);
          return new RelocationResult(updated, relocation);
        });
  }

  private Shelf withOccupied(Shelf shelf, int occupied) {
    return new Shelf(
        shelf.id(),
        shelf.shelfCode(),
        shelf.zone(),
        shelf.capacity(),
        occupied,
        shelf.status(),
        shelf.createdAt());
  }
}
