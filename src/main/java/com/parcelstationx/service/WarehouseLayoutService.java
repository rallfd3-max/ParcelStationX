package com.parcelstationx.service;

import com.parcelstationx.dao.ParcelDao;
import com.parcelstationx.dao.ShelfDao;
import com.parcelstationx.dao.ShelfLayoutDao;
import com.parcelstationx.dao.ShelfSlotDao;
import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.ShelfLayout;
import com.parcelstationx.model.ShelfSlot;
import java.time.LocalDateTime;

public final class WarehouseLayoutService {
  private final ShelfDao shelves;
  private final ShelfLayoutDao layouts;
  private final ShelfSlotDao slots;
  private final ParcelDao parcels;

  public WarehouseLayoutService(
      ShelfDao shelves, ShelfLayoutDao layouts, ShelfSlotDao slots, ParcelDao parcels) {
    this.shelves = shelves;
    this.layouts = layouts;
    this.slots = slots;
    this.parcels = parcels;
  }

  public WarehouseSnapshot snapshot() {
    return new WarehouseSnapshot(
        shelves.findAll(), layouts.findAll(), slots.findAll(), parcels.findAll());
  }

  public ShelfLayout updateLayout(ShelfLayout value) {
    if (value == null
        || value.shelfId() == null
        || value.width() <= 0
        || value.height() <= 0
        || value.depth() <= 0
        || value.columns() <= 0
        || value.levels() <= 0) {
      throw new BusinessException("布局尺寸、列数和层数必须为正数。");
    }
    shelves.findById(value.shelfId()).orElseThrow(() -> new BusinessException("货架不存在。"));
    return layouts.save(
        new ShelfLayout(
            value.shelfId(),
            value.positionX(),
            value.positionY(),
            value.positionZ(),
            value.rotationY(),
            value.width(),
            value.height(),
            value.depth(),
            value.columns(),
            value.levels(),
            LocalDateTime.now()));
  }

  public ShelfSlot setSlotEnabled(long slotId, boolean enabled) {
    ShelfSlot slot = slots.findById(slotId).orElseThrow(() -> new BusinessException("仓位不存在。"));
    if (!enabled && parcels.findAll().stream().anyMatch(p -> slot.id().equals(p.slotId()))) {
      throw new BusinessException("占用中的仓位不能禁用。");
    }
    return slots.save(
        new ShelfSlot(
            slot.id(),
            slot.shelfId(),
            slot.slotCode(),
            slot.levelIndex(),
            slot.columnIndex(),
            enabled,
            slot.createdAt()));
  }
}
