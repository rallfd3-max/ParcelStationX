package com.parcelstationx.api.dto;

public record CreateShelfRequest(
    String shelfCode,
    String zone,
    Integer levels,
    Integer columns,
    Double width,
    Double height,
    Double depth) {}
