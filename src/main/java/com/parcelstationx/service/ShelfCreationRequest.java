package com.parcelstationx.service;

public record ShelfCreationRequest(
    String shelfCode,
    String zone,
    int count,
    int levels,
    int columns,
    double width,
    double height,
    double depth) {}
