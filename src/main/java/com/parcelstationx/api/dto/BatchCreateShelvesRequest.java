package com.parcelstationx.api.dto;

public record BatchCreateShelvesRequest(
    String zone,
    Integer count,
    Integer levels,
    Integer columns,
    Double width,
    Double height,
    Double depth,
    String layoutMode,
    Integer maxShelvesPerRow,
    Double shelfGap,
    Double aisleGap) {}
