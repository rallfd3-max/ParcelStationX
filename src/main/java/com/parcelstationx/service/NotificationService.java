package com.parcelstationx.service;

import com.parcelstationx.dao.CustomerDao;
import com.parcelstationx.dao.NotificationRecordDao;
import com.parcelstationx.dao.ParcelDao;
import com.parcelstationx.model.*;
import com.parcelstationx.task.NotificationQueue;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public final class NotificationService implements AutoCloseable {
  private final NotificationRecordDao records;
  private final NotificationQueue queue;
  private final CustomerDao customers;
  private final NotificationGateway gateway;
  private final ParcelDao parcels;
  private final NotificationContentService content;
  private final int maxRetries;

  public NotificationService(
      NotificationRecordDao records, CustomerDao customers, NotificationQueue queue) {
    this(records, customers, queue, record -> {}, null, new NotificationContentService(null), 3);
  }

  public NotificationService(
      NotificationRecordDao records,
      CustomerDao customers,
      NotificationQueue queue,
      NotificationGateway gateway) {
    this(records, customers, queue, gateway, null, new NotificationContentService(null), 3);
  }

  public NotificationService(
      NotificationRecordDao records,
      CustomerDao customers,
      NotificationQueue queue,
      NotificationGateway gateway,
      ParcelDao parcels,
      NotificationContentService content,
      int maxRetries) {
    this.records = records;
    this.customers = customers;
    this.queue = queue;
    this.gateway = gateway;
    this.parcels = parcels;
    this.content = content;
    this.maxRetries = maxRetries;
  }

  public CompletableFuture<Void> notifyInbound(Parcel parcel) {
    return notifyInbound(parcel, customers.findById(parcel.customerId()).orElseThrow());
  }

  public CompletableFuture<Void> notifyInbound(Parcel parcel, Customer customer) {
    NotificationRecord pending =
        records.save(
            new NotificationRecord(
                null,
                parcel.id(),
                customer.id(),
                "INBOUND",
                customer.mobile(),
                content.inbound(parcel),
                NotificationStatus.PENDING,
                0,
                LocalDateTime.now(),
                null,
                null));
    return queue.submit(() -> send(pending));
  }

  public synchronized CompletableFuture<Void> notifyOverdue(Parcel parcel, int days) {
    String type = "OVERDUE_DAY_" + days;
    if (records.existsByParcelAndType(parcel.id(), type))
      return CompletableFuture.completedFuture(null);
    Customer customer = customers.findById(parcel.customerId()).orElseThrow();
    NotificationRecord pending =
        records.save(
            new NotificationRecord(
                null,
                parcel.id(),
                customer.id(),
                type,
                customer.mobile(),
                content.overdue(parcel, days),
                NotificationStatus.PENDING,
                0,
                LocalDateTime.now(),
                null,
                null));
    return queue.submit(() -> send(pending));
  }

  public CompletableFuture<Void> retry(long id) {
    NotificationRecord record = records.findById(id).orElseThrow();
    if (record.retryCount() >= maxRetries) {
      return CompletableFuture.failedFuture(
          new IllegalStateException("Notification retry limit reached."));
    }
    return queue.submit(
        () ->
            send(
                new NotificationRecord(
                    record.id(),
                    record.parcelId(),
                    record.customerId(),
                    record.notificationType(),
                    record.target(),
                    record.content(),
                    NotificationStatus.PENDING,
                    record.retryCount() + 1,
                    record.createdAt(),
                    null,
                    null)));
  }

  private void send(NotificationRecord record) {
    if (record.notificationType().startsWith("OVERDUE_DAY_") && !stillInStock(record.parcelId())) {
      records.save(
          withStatus(record, NotificationStatus.FAILED, null, "CANCELED_PARCEL_NOT_IN_STOCK"));
      return;
    }
    try {
      gateway.send(record);
      records.save(withStatus(record, NotificationStatus.SUCCESS, LocalDateTime.now(), null));
    } catch (RuntimeException failure) {
      records.save(withStatus(record, NotificationStatus.FAILED, null, safeError(failure)));
    }
  }

  private boolean stillInStock(long parcelId) {
    return parcels != null
        && parcels.findById(parcelId).map(p -> p.status() == ParcelStatus.IN_STOCK).orElse(false);
  }

  private static NotificationRecord withStatus(
      NotificationRecord record, NotificationStatus status, LocalDateTime sentAt, String error) {
    return new NotificationRecord(
        record.id(),
        record.parcelId(),
        record.customerId(),
        record.notificationType(),
        record.target(),
        record.content(),
        status,
        record.retryCount(),
        record.createdAt(),
        sentAt,
        error);
  }

  private static String safeError(RuntimeException failure) {
    String name = failure.getClass().getSimpleName();
    return name.length() > 250 ? "NotificationGatewayFailure" : name;
  }

  public void close() {
    queue.close();
  }
}
