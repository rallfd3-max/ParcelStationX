package com.parcelstationx.model;
import java.time.LocalDateTime;
public record Parcel(Long id, String trackingNo, String courierCompany, Long customerId, Long shelfId, String pickupCode, ParcelStatus status, LocalDateTime arrivedAt, LocalDateTime pickedUpAt, Long operatorId, String remark, LocalDateTime createdAt, LocalDateTime updatedAt) { }
