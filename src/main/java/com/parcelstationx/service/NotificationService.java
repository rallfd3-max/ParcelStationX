package com.parcelstationx.service;

import com.parcelstationx.dao.CustomerDao;
import com.parcelstationx.dao.NotificationRecordDao;
import com.parcelstationx.model.*;
import com.parcelstationx.task.NotificationQueue;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public final class NotificationService implements AutoCloseable {
  private final NotificationRecordDao records;
  private final NotificationQueue queue;
  private final CustomerDao customers;

  public NotificationService(
      NotificationRecordDao records, CustomerDao customers, NotificationQueue queue) {
    this.records = records;
    this.customers = customers;
    this.queue = queue;
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
                "取件码：" + parcel.pickupCode(),
                NotificationStatus.PENDING,
                0,
                LocalDateTime.now(),
                null,
                null));
    return queue.submit(() -> send(pending));
  }

  public CompletableFuture<Void> retry(long id) {
    NotificationRecord record = records.findById(id).orElseThrow();
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
    try {
      records.save(
          new NotificationRecord(
              record.id(),
              record.parcelId(),
              record.customerId(),
              record.notificationType(),
              record.target(),
              record.content(),
              NotificationStatus.SUCCESS,
              record.retryCount(),
              record.createdAt(),
              LocalDateTime.now(),
              null));
    } catch (RuntimeException failure) {
      records.save(
          new NotificationRecord(
              record.id(),
              record.parcelId(),
              record.customerId(),
              record.notificationType(),
              record.target(),
              record.content(),
              NotificationStatus.FAILED,
              record.retryCount(),
              record.createdAt(),
              null,
              failure.getMessage()));
    }
  }

  public void close() {
    queue.close();
  }
}
