package com.parcelstationx.api.dto;

public record ResizeShelfRequest(
    Integer levels, Integer columns, Double width, Double height, Double depth) {}
