package com.parcelstationx.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.parcelstationx.dao.*;
import com.parcelstationx.model.*;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;

class StatisticsServiceTest {
  @Test
  void calculatesOperationalMetrics() {
    LocalDateTime now = LocalDateTime.now();
    Parcel p =
        new Parcel(
            1L,
            "T",
            "SF",
            1L,
            1L,
            "1",
            ParcelStatus.IN_STOCK,
            now.minusDays(8),
            null,
            1L,
            null,
            now,
            now);
    StatisticsSnapshot s =
        new StatisticsService(
                new Parcels(List.of(p)),
                new Shelves(List.of(new Shelf(1L, "A", "A", 10, 5, ShelfStatus.ACTIVE, now))),
                new Exceptions())
            .calculate();
    assertEquals(1, s.inventory());
    assertEquals(1, s.overdue());
    assertEquals(50d, s.shelfOccupancy().get("A"));
  }

  private abstract static class ReadDao<T> implements BaseDao<T, Long> {
    final List<T> values;

    ReadDao(List<T> values) {
      this.values = values;
    }

    public T save(T v) {
      return v;
    }

    public Optional<T> findById(Long id) {
      return Optional.empty();
    }

    public List<T> findAll() {
      return values;
    }

    public boolean deleteById(Long id) {
      return false;
    }
  }

  private static final class Parcels extends ReadDao<Parcel> implements ParcelDao {
    Parcels(List<Parcel> v) {
      super(v);
    }

    public Optional<Parcel> findByTrackingNo(String v) {
      return Optional.empty();
    }

    public Optional<Parcel> findByPickupCode(String v) {
      return Optional.empty();
    }
  }

  private static final class Shelves extends ReadDao<Shelf> implements ShelfDao {
    Shelves(List<Shelf> v) {
      super(v);
    }

    public Optional<Shelf> findAvailable() {
      return Optional.empty();
    }
  }

  private static final class Exceptions extends ReadDao<ExceptionRecord>
      implements ExceptionRecordDao {
    Exceptions() {
      super(List.of());
    }

    public List<ExceptionRecord> findOpen() {
      return List.of();
    }
  }
}
