package com.parcelstationx.service;

import static org.junit.jupiter.api.Assertions.*;

import com.parcelstationx.model.ShelfLayout;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ShelfAutoLayoutServiceTest {
  private final ShelfAutoLayoutService service = new ShelfAutoLayoutService();

  @Test
  void allModesAreDeterministicAndNonOverlapping() {
    for (ShelfLayoutMode mode : ShelfLayoutMode.values()) {
      var request = new ShelfAutoLayoutRequest(8, 3.6, 2.6, .8, mode, 4, .6, 2.5);
      List<ShelfLayout> first = service.plan(List.of(), request, LocalDateTime.MIN);
      List<ShelfLayout> second = service.plan(List.of(), request, LocalDateTime.MIN);
      assertEquals(first, second);
      for (int i = 0; i < first.size(); i++) {
        assertFalse(service.collides(first.get(i), first.subList(0, i)));
      }
    }
  }

  @Test
  void wideMainAislePreservesConfiguredGap() {
    var request =
        new ShelfAutoLayoutRequest(2, 3.6, 2.6, .8, ShelfLayoutMode.WIDE_MAIN_AISLE, 1, .6, 3.2);
    List<ShelfLayout> result = service.plan(List.of(), request, LocalDateTime.MIN);
    double clearGap = result.get(1).positionZ() - result.get(0).positionZ() - .8;
    assertEquals(3.2, clearGap, .0001);
  }

  @Test
  void plansAfterExistingFootprint() {
    ShelfLayout existing = new ShelfLayout(1L, 0, 0, 5, 0, 4, 2, 1, 2, 2, LocalDateTime.MIN);
    var request = new ShelfAutoLayoutRequest(2, 4, 2.6, 1, ShelfLayoutMode.GRID, 4, .6, 2.5);
    List<ShelfLayout> result = service.plan(List.of(existing), request, LocalDateTime.MIN);
    assertFalse(service.collides(result.get(0), List.of(existing)));
  }
}
