package com.parcelstationx.api.dto;

import com.parcelstationx.model.Parcel;
import com.parcelstationx.model.ParcelStatus;
import com.parcelstationx.service.WarehouseSnapshot;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record DashboardDto(
    long todayInbound,
    long todayOutbound,
    long inventory,
    long exceptions,
    long overdue,
    double slotUtilization,
    Map<String, Long> courierVolumes,
    Map<String, Double> shelfOccupancy) {
  public static DashboardDto from(WarehouseSnapshot snapshot) {
    LocalDate today = LocalDate.now();
    long inbound =
        snapshot.parcels().stream().filter(p -> p.arrivedAt().toLocalDate().equals(today)).count();
    long outbound =
        snapshot.parcels().stream()
            .filter(p -> p.pickedUpAt() != null && p.pickedUpAt().toLocalDate().equals(today))
            .count();
    long inventory =
        snapshot.parcels().stream().filter(p -> p.status() == ParcelStatus.IN_STOCK).count();
    long exceptions =
        snapshot.parcels().stream().filter(p -> p.status() == ParcelStatus.EXCEPTION).count();
    long overdue =
        snapshot.parcels().stream()
            .filter(
                p ->
                    p.status() == ParcelStatus.IN_STOCK
                        && p.arrivedAt().isBefore(LocalDateTime.now().minusDays(7)))
            .count();
    long enabledSlots = snapshot.slots().stream().filter(slot -> slot.enabled()).count();
    long usedSlots =
        snapshot.parcels().stream()
            .filter(p -> p.slotId() != null && p.status() != ParcelStatus.PICKED_UP)
            .count();
    Map<String, Long> courier =
        snapshot.parcels().stream()
            .collect(
                Collectors.groupingBy(
                    Parcel::courierCompany, LinkedHashMap::new, Collectors.counting()));
    Map<String, Double> shelfOccupancy = new LinkedHashMap<>();
    snapshot
        .shelves()
        .forEach(
            shelf ->
                shelfOccupancy.put(
                    shelf.shelfCode(),
                    shelf.capacity() == 0
                        ? 0
                        : Math.round(1000d * shelf.occupied() / shelf.capacity()) / 10d));
    return new DashboardDto(
        inbound,
        outbound,
        inventory,
        exceptions,
        overdue,
        enabledSlots == 0 ? 0 : Math.round(1000d * usedSlots / enabledSlots) / 10d,
        courier,
        shelfOccupancy);
  }
}
