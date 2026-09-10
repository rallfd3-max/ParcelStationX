package com.parcelstationx.service;

import java.util.Map;

public record StatisticsSnapshot(
    long todayInbound,
    long todayOutbound,
    long inventory,
    long exceptions,
    long overdue,
    double averageStayHours,
    Map<String, Long> courierVolumes,
    Map<String, Double> shelfOccupancy) {}
