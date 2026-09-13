package com.parcelstationx.api.dto;

import com.parcelstationx.model.Shelf;
import com.parcelstationx.model.ShelfLayout;
import com.parcelstationx.model.ShelfSlot;
import com.parcelstationx.service.WarehouseSnapshot;
import java.util.List;

public record WarehouseDto(
    List<Shelf> shelves,
    List<ShelfLayout> layouts,
    List<ShelfSlot> slots,
    List<ParcelDto> parcels) {
  public static WarehouseDto from(WarehouseSnapshot snapshot) {
    return new WarehouseDto(
        snapshot.shelves(),
        snapshot.layouts(),
        snapshot.slots(),
        snapshot.parcels().stream().map(ParcelDto::from).toList());
  }
}
