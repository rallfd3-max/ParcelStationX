package com.parcelstationx.task;

import com.parcelstationx.config.NotificationConfig;
import com.parcelstationx.dao.ParcelDao;
import com.parcelstationx.model.ParcelStatus;
import com.parcelstationx.service.NotificationService;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class OverdueNotificationScheduler implements AutoCloseable {
  private final ParcelDao parcels;
  private final NotificationService notifications;
  private final NotificationConfig config;
  private final Clock clock;
  private final ScheduledExecutorService executor;

  public OverdueNotificationScheduler(
      ParcelDao parcels,
      NotificationService notifications,
      NotificationConfig config,
      Clock clock) {
    this(
        parcels,
        notifications,
        config,
        clock,
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              Thread thread = new Thread(r, "parcel-overdue-notifications");
              thread.setDaemon(true);
              return thread;
            }));
  }

  OverdueNotificationScheduler(
      ParcelDao parcels,
      NotificationService notifications,
      NotificationConfig config,
      Clock clock,
      ScheduledExecutorService executor) {
    this.parcels = parcels;
    this.notifications = notifications;
    this.config = config;
    this.clock = clock;
    this.executor = executor;
  }

  public void runOnce() {
    LocalDateTime now = LocalDateTime.now(clock);
    parcels.findAll().stream()
        .filter(parcel -> parcel.status() == ParcelStatus.IN_STOCK)
        .forEach(
            parcel -> {
              long dwellDays = Math.max(0, Duration.between(parcel.arrivedAt(), now).toDays());
              config.overdueDays().stream()
                  .filter(stage -> dwellDays >= stage)
                  .forEach(stage -> notifications.notifyOverdue(parcel, stage));
            });
  }

  public void start() {
    executor.scheduleWithFixedDelay(this::safeRun, 0, config.scanMinutes(), TimeUnit.MINUTES);
  }

  private void safeRun() {
    try {
      runOnce();
    } catch (RuntimeException ignored) {
      // A scanner failure must not terminate future scheduled scans or core application work.
    }
  }

  @Override
  public void close() {
    executor.shutdownNow();
  }

  public boolean isShutdown() {
    return executor.isShutdown();
  }
}
