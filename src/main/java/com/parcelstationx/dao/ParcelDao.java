package com.parcelstationx.dao;

import com.parcelstationx.model.Parcel;
import java.sql.Connection;
import java.util.Optional;

public interface ParcelDao extends BaseDao<Parcel, Long> {
  Optional<Parcel> findByTrackingNo(String trackingNo);

  Optional<Parcel> findByPickupCode(String pickupCode);

  default Optional<Parcel> findBySlotId(Connection connection, long slotId) {
    throw new UnsupportedOperationException("Connection-aware slot lookup is not implemented.");
  }

  default boolean assignSlot(
      Connection connection, long parcelId, Long shelfId, Long slotId, long version) {
    throw new UnsupportedOperationException("Slot assignment is not implemented.");
  }
}
