package com.parcelstationx.backup;

import com.parcelstationx.model.*;
import java.io.Serializable;
import java.util.List;

public record BackupSnapshot(
    BackupMetadata metadata,
    List<Customer> customers,
    List<Shelf> shelves,
    List<Parcel> parcels,
    List<ParcelEvent> events,
    List<ExceptionRecord> exceptions,
    List<NotificationRecord> notifications,
    List<OperationLog> operationLogs)
    implements Serializable {
  private static final long serialVersionUID = 1L;

  public BackupSnapshot {
    customers = List.copyOf(customers);
    shelves = List.copyOf(shelves);
    parcels = List.copyOf(parcels);
    events = List.copyOf(events);
    exceptions = List.copyOf(exceptions);
    notifications = List.copyOf(notifications);
    operationLogs = List.copyOf(operationLogs);
  }
}
