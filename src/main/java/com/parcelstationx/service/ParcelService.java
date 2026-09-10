package com.parcelstationx.service;

import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.Parcel;
import com.parcelstationx.model.ParcelStatus;

public final class ParcelService {
  private final ParcelStateMachine states = new ParcelStateMachine();

  public void validateInbound(String trackingNo, String mobile) {
    if (trackingNo == null || !trackingNo.matches("[A-Za-z0-9-]{6,100}"))
      throw new BusinessException("Tracking number format is invalid.");
    if (mobile == null || !mobile.matches("1\\d{10}"))
      throw new BusinessException("Mobile number format is invalid.");
  }

  public ParcelStatus pickup(Parcel parcel, String pickupCode) {
    if (!parcel.pickupCode().equals(pickupCode))
      throw new BusinessException("Pickup code is incorrect.");
    states.requireTransition(parcel.status(), ParcelStatus.PICKED_UP);
    return ParcelStatus.PICKED_UP;
  }
}
