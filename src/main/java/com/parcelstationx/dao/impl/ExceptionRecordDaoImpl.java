package com.parcelstationx.dao.impl;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.ExceptionRecordDao;
import com.parcelstationx.model.*;
import java.util.List;

public final class ExceptionRecordDaoImpl extends AbstractJdbcDao<ExceptionRecord>
    implements ExceptionRecordDao {
  private static final String C =
      "id,parcel_id,exception_type,description,status,created_by,handled_by,created_at,handled_at,resolution";

  public ExceptionRecordDaoImpl(ConnectionProvider p) {
    super(
        p,
        "INSERT INTO exception_records(parcel_id,exception_type,description,status,created_by,handled_by,created_at,handled_at,resolution) VALUES(?,?,?,?,?,?,?,?,?)",
        "UPDATE exception_records SET parcel_id=?,exception_type=?,description=?,status=?,created_by=?,handled_by=?,created_at=?,handled_at=?,resolution=? WHERE id=?",
        "SELECT " + C + " FROM exception_records ORDER BY id DESC",
        "SELECT " + C + " FROM exception_records WHERE id=?",
        "DELETE FROM exception_records WHERE id=?",
        ExceptionRecordDaoImpl::bind,
        (s, v) -> {
          bind(s, v);
          s.setLong(10, v.id());
        },
        r ->
            new ExceptionRecord(
                r.getLong("id"),
                r.getLong("parcel_id"),
                ExceptionType.valueOf(r.getString("exception_type")),
                r.getString("description"),
                r.getString("status"),
                r.getLong("created_by"),
                JdbcValues.nullableLong(r, "handled_by"),
                JdbcValues.time(r, "created_at"),
                JdbcValues.time(r, "handled_at"),
                r.getString("resolution")));
  }

  @Override
  public List<ExceptionRecord> findOpen() {
    return query(
        "SELECT " + C + " FROM exception_records WHERE status='OPEN' ORDER BY id", (s, v) -> {});
  }

  @Override
  protected Long idOf(ExceptionRecord v) {
    return v.id();
  }

  @Override
  protected ExceptionRecord withId(ExceptionRecord v, long id) {
    return new ExceptionRecord(
        id,
        v.parcelId(),
        v.exceptionType(),
        v.description(),
        v.status(),
        v.createdBy(),
        v.handledBy(),
        v.createdAt(),
        v.handledAt(),
        v.resolution());
  }

  private static void bind(java.sql.PreparedStatement s, ExceptionRecord v)
      throws java.sql.SQLException {
    s.setLong(1, v.parcelId());
    s.setString(2, v.exceptionType().name());
    s.setString(3, v.description());
    s.setString(4, v.status());
    s.setLong(5, v.createdBy());
    JdbcValues.nullableLong(s, 6, v.handledBy());
    JdbcValues.time(s, 7, v.createdAt());
    JdbcValues.time(s, 8, v.handledAt());
    s.setString(9, v.resolution());
  }
}
