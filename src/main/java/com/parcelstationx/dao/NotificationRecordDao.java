package com.parcelstationx.dao;

import com.parcelstationx.model.NotificationRecord;
import com.parcelstationx.model.NotificationStatus;
import java.util.List;

public interface NotificationRecordDao extends BaseDao<NotificationRecord, Long> {
  List<NotificationRecord> findByStatus(NotificationStatus status);
}
