package com.parcelstationx.service;

public record ShelfAutoLayoutRequest(
    int count,
    double width,
    double height,
    double depth,
    ShelfLayoutMode mode,
    int maxShelvesPerRow,
    double shelfGap,
    double aisleGap) {}
