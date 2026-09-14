package com.parcelstationx.service;

import java.util.List;

public record ShelfCreationResult(
    List<ShelfCreationItem> shelves, int shelfCount, int slotCount, boolean preview) {
  public ShelfCreationResult {
    shelves = List.copyOf(shelves);
  }
}
