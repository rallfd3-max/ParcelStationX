package com.parcelstationx.dao.impl;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.NotificationRecordDao;
import com.parcelstationx.model.*;
import java.util.List;

public final class NotificationRecordDaoImpl extends AbstractJdbcDao<NotificationRecord>
    implements NotificationRecordDao {
  private static final String C =
      "id,parcel_id,customer_id,notification_type,target,content,status,retry_count,created_at,sent_at,error_message";

  public NotificationRecordDaoImpl(ConnectionProvider p) {
    super(
        p,
        "INSERT INTO notification_records(parcel_id,customer_id,notification_type,target,content,status,retry_count,created_at,sent_at,error_message) VALUES(?,?,?,?,?,?,?,?,?,?)",
        "UPDATE notification_records SET parcel_id=?,customer_id=?,notification_type=?,target=?,content=?,status=?,retry_count=?,created_at=?,sent_at=?,error_message=? WHERE id=?",
        "SELECT " + C + " FROM notification_records ORDER BY id DESC",
        "SELECT " + C + " FROM notification_records WHERE id=?",
        "DELETE FROM notification_records WHERE id=?",
        NotificationRecordDaoImpl::bind,
        (s, v) -> {
          bind(s, v);
          s.setLong(11, v.id());
        },
        r ->
            new NotificationRecord(
                r.getLong("id"),
                r.getLong("parcel_id"),
                r.getLong("customer_id"),
                r.getString("notification_type"),
                r.getString("target"),
                r.getString("content"),
                NotificationStatus.valueOf(r.getString("status")),
                r.getInt("retry_count"),
                JdbcValues.time(r, "created_at"),
                JdbcValues.time(r, "sent_at"),
                r.getString("error_message")));
  }

  @Override
  public List<NotificationRecord> findByStatus(NotificationStatus status) {
    return query(
        "SELECT " + C + " FROM notification_records WHERE status=? ORDER BY id",
        (s, v) -> s.setString(1, status.name()));
  }

  @Override
  protected Long idOf(NotificationRecord v) {
    return v.id();
  }

  @Override
  protected NotificationRecord withId(NotificationRecord v, long id) {
    return new NotificationRecord(
        id,
        v.parcelId(),
        v.customerId(),
        v.notificationType(),
        v.target(),
        v.content(),
        v.status(),
        v.retryCount(),
        v.createdAt(),
        v.sentAt(),
        v.errorMessage());
  }

  private static void bind(java.sql.PreparedStatement s, NotificationRecord v)
      throws java.sql.SQLException {
    s.setLong(1, v.parcelId());
    s.setLong(2, v.customerId());
    s.setString(3, v.notificationType());
    s.setString(4, v.target());
    s.setString(5, v.content());
    s.setString(6, v.status().name());
    s.setInt(7, v.retryCount());
    JdbcValues.time(s, 8, v.createdAt());
    JdbcValues.time(s, 9, v.sentAt());
    s.setString(10, v.errorMessage());
  }
}
