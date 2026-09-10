package com.parcelstationx.backup;

import com.parcelstationx.dao.impl.*;
import com.parcelstationx.exception.*;
import com.parcelstationx.service.TransactionRunner;
import java.time.LocalDateTime;

public final class JdbcBackupDataStore implements BackupDataStore {
  private static final String VERSION = "1";
  private final TransactionRunner tx;
  private final CustomerDaoImpl customers;
  private final ShelfDaoImpl shelves;
  private final ParcelDaoImpl parcels;
  private final ParcelEventDaoImpl events;
  private final ExceptionRecordDaoImpl exceptions;
  private final NotificationRecordDaoImpl notifications;
  private final OperationLogDaoImpl logs;

  public JdbcBackupDataStore(
      TransactionRunner tx,
      CustomerDaoImpl customers,
      ShelfDaoImpl shelves,
      ParcelDaoImpl parcels,
      ParcelEventDaoImpl events,
      ExceptionRecordDaoImpl exceptions,
      NotificationRecordDaoImpl notifications,
      OperationLogDaoImpl logs) {
    this.tx = tx;
    this.customers = customers;
    this.shelves = shelves;
    this.parcels = parcels;
    this.events = events;
    this.exceptions = exceptions;
    this.notifications = notifications;
    this.logs = logs;
  }

  public BackupSnapshot snapshot() {
    var c = customers.findAll();
    var s = shelves.findAll();
    var p = parcels.findAll();
    var e = events.findAll();
    var x = exceptions.findAll();
    var n = notifications.findAll();
    var l = logs.findAll();
    long count = c.size() + s.size() + p.size() + e.size() + x.size() + n.size() + l.size();
    return new BackupSnapshot(
        new BackupMetadata(
            VERSION, LocalDateTime.now(), count, Integer.toHexString(Long.hashCode(count))),
        c,
        s,
        p,
        e,
        x,
        n,
        l);
  }

  public void restore(BackupSnapshot value) {
    if (!VERSION.equals(value.metadata().version()))
      throw new AppException("Backup version is not supported.");
    tx.run(
        c -> {
          try (var s = c.createStatement()) {
            for (String table :
                new String[] {
                  "notification_records",
                  "exception_records",
                  "parcel_events",
                  "operation_logs",
                  "parcels",
                  "shelves",
                  "customers"
                }) s.executeUpdate("DELETE FROM " + table);
          } catch (java.sql.SQLException e) {
            throw new DatabaseException("Backup restore cleanup failed.", e);
          }
          value.customers().forEach(v -> customers.restore(c, v));
          value.shelves().forEach(v -> shelves.restore(c, v));
          value.parcels().forEach(v -> parcels.restore(c, v));
          value.events().forEach(v -> events.restore(c, v));
          value.exceptions().forEach(v -> exceptions.restore(c, v));
          value.notifications().forEach(v -> notifications.restore(c, v));
          value.operationLogs().forEach(v -> logs.restore(c, v));
          return null;
        });
  }
}
