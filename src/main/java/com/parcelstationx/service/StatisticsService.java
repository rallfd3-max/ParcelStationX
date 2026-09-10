package com.parcelstationx.service;

import com.parcelstationx.dao.*;
import com.parcelstationx.model.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public final class StatisticsService {
  private final ParcelDao parcels;
  private final ShelfDao shelves;
  private final ExceptionRecordDao exceptions;

  public StatisticsService(ParcelDao parcels, ShelfDao shelves, ExceptionRecordDao exceptions) {
    this.parcels = parcels;
    this.shelves = shelves;
    this.exceptions = exceptions;
  }

  public StatisticsSnapshot calculate() {
    LocalDate today = LocalDate.now();
    List<Parcel> all = parcels.findAll();
    long inbound = all.stream().filter(p -> p.arrivedAt().toLocalDate().equals(today)).count();
    long outbound =
        all.stream()
            .filter(p -> p.pickedUpAt() != null && p.pickedUpAt().toLocalDate().equals(today))
            .count();
    List<Parcel> stock = all.stream().filter(p -> p.status() == ParcelStatus.IN_STOCK).toList();
    long overdue =
        stock.stream()
            .filter(p -> p.arrivedAt().isBefore(LocalDateTime.now().minusDays(7)))
            .count();
    double average =
        stock.stream()
            .mapToLong(p -> Duration.between(p.arrivedAt(), LocalDateTime.now()).toHours())
            .average()
            .orElse(0);
    Map<String, Long> courier =
        all.stream()
            .collect(
                Collectors.groupingBy(
                    Parcel::courierCompany, LinkedHashMap::new, Collectors.counting()));
    Map<String, Double> occupancy = new LinkedHashMap<>();
    for (Shelf s : shelves.findAll())
      occupancy.put(
          s.shelfCode(),
          Math.round((s.capacity() == 0 ? 0d : 100d * s.occupied() / s.capacity()) * 10d) / 10d);
    return new StatisticsSnapshot(
        inbound,
        outbound,
        stock.size(),
        exceptions.findOpen().size(),
        overdue,
        average,
        courier,
        occupancy);
  }
}
