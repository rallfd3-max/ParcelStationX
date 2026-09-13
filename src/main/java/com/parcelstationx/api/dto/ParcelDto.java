package com.parcelstationx.api.dto;

import com.parcelstationx.model.Parcel;

public record ParcelDto(
    Long id,
    String trackingNo,
    String courierCompany,
    Long customerId,
    Long shelfId,
    String status,
    String arrivedAt,
    String pickedUpAt,
    Long operatorId,
    String remark) {
  public static ParcelDto from(Parcel parcel) {
    return new ParcelDto(
        parcel.id(),
        parcel.trackingNo(),
        parcel.courierCompany(),
        parcel.customerId(),
        parcel.shelfId(),
        parcel.status().name(),
        parcel.arrivedAt().toString(),
        parcel.pickedUpAt() == null ? null : parcel.pickedUpAt().toString(),
        parcel.operatorId(),
        parcel.remark());
  }
}
