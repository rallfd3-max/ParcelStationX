package com.parcelstationx.dao;

import com.parcelstationx.model.NotificationRecord;
import com.parcelstationx.model.NotificationStatus;
import java.util.List;
import java.util.Optional;

public interface NotificationRecordDao extends BaseDao<NotificationRecord, Long> {
  List<NotificationRecord> findByStatus(NotificationStatus status);

  Optional<NotificationRecord> findByParcelAndType(long parcelId, String notificationType);

  default boolean existsByParcelAndType(long parcelId, String notificationType) {
    return findByParcelAndType(parcelId, notificationType).isPresent();
  }
}
