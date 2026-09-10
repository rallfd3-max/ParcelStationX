package com.parcelstationx.dao;

import com.parcelstationx.model.Parcel;
import java.util.Optional;

public interface ParcelDao extends BaseDao<Parcel, Long> {
  Optional<Parcel> findByTrackingNo(String trackingNo);

  Optional<Parcel> findByPickupCode(String pickupCode);
}
