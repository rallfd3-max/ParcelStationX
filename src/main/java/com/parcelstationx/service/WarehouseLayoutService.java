package com.parcelstationx.service;

import com.parcelstationx.dao.ParcelDao;
import com.parcelstationx.dao.ShelfDao;
import com.parcelstationx.dao.ShelfLayoutDao;
import com.parcelstationx.dao.ShelfSlotDao;

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
}
