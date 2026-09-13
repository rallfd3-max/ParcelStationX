package com.parcelstationx.model;

import java.time.LocalDateTime;

public record Parcel(
    Long id,
    String trackingNo,
    String courierCompany,
    Long customerId,
    Long shelfId,
    String pickupCode,
    ParcelStatus status,
    LocalDateTime arrivedAt,
    LocalDateTime pickedUpAt,
    Long operatorId,
    String remark,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long slotId,
    long version)
    implements java.io.Serializable {
  public Parcel(
      Long id,
      String trackingNo,
      String courierCompany,
      Long customerId,
      Long shelfId,
      String pickupCode,
      ParcelStatus status,
      LocalDateTime arrivedAt,
      LocalDateTime pickedUpAt,
      Long operatorId,
      String remark,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this(
        id,
        trackingNo,
        courierCompany,
        customerId,
        shelfId,
        pickupCode,
        status,
        arrivedAt,
        pickedUpAt,
        operatorId,
        remark,
        createdAt,
        updatedAt,
        null,
        0L);
  }
}
