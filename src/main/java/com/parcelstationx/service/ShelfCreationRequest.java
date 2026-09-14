package com.parcelstationx.service;

public record ShelfCreationRequest(
    String shelfCode,
    String zone,
    int count,
    int levels,
    int columns,
    double width,
    double height,
    double depth,
    ShelfLayoutMode layoutMode,
    int maxShelvesPerRow,
    double shelfGap,
    double aisleGap) {
  public ShelfCreationRequest(
      String shelfCode,
      String zone,
      int count,
      int levels,
      int columns,
      double width,
      double height,
      double depth) {
    this(
        shelfCode,
        zone,
        count,
        levels,
        columns,
        width,
        height,
        depth,
        ShelfLayoutMode.GRID,
        4,
        .6,
        2.5);
  }
}
