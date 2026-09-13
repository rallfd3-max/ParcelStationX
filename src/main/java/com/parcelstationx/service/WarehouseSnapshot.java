package com.parcelstationx.service;

import com.parcelstationx.model.Parcel;
import com.parcelstationx.model.Shelf;
import com.parcelstationx.model.ShelfLayout;
import com.parcelstationx.model.ShelfSlot;
import java.util.List;

public record WarehouseSnapshot(
    List<Shelf> shelves, List<ShelfLayout> layouts, List<ShelfSlot> slots, List<Parcel> parcels) {
  public WarehouseSnapshot {
    shelves = List.copyOf(shelves);
    layouts = List.copyOf(layouts);
    slots = List.copyOf(slots);
    parcels = List.copyOf(parcels);
  }
}
