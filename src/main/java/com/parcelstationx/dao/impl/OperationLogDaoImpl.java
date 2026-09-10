package com.parcelstationx.dao.impl;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.OperationLogDao;
import com.parcelstationx.model.OperationLog;

public final class OperationLogDaoImpl extends AbstractJdbcDao<OperationLog>
    implements OperationLogDao {
  private static final String C =
      "id,user_id,operation_type,target_type,target_id,description,created_at";

  public OperationLogDaoImpl(ConnectionProvider p) {
    super(
        p,
        "INSERT INTO operation_logs(user_id,operation_type,target_type,target_id,description,created_at) VALUES(?,?,?,?,?,?)",
        "UPDATE operation_logs SET user_id=?,operation_type=?,target_type=?,target_id=?,description=?,created_at=? WHERE id=?",
        "SELECT " + C + " FROM operation_logs ORDER BY id DESC",
        "SELECT " + C + " FROM operation_logs WHERE id=?",
        "DELETE FROM operation_logs WHERE id=?",
        OperationLogDaoImpl::bind,
        (s, v) -> {
          bind(s, v);
          s.setLong(7, v.id());
        },
        r ->
            new OperationLog(
                r.getLong("id"),
                JdbcValues.nullableLong(r, "user_id"),
                r.getString("operation_type"),
                r.getString("target_type"),
                JdbcValues.nullableLong(r, "target_id"),
                r.getString("description"),
                JdbcValues.time(r, "created_at")));
  }

  @Override
  protected Long idOf(OperationLog v) {
    return v.id();
  }

  @Override
  protected OperationLog withId(OperationLog v, long id) {
    return new OperationLog(
        id,
        v.userId(),
        v.operationType(),
        v.targetType(),
        v.targetId(),
        v.description(),
        v.createdAt());
  }

  private static void bind(java.sql.PreparedStatement s, OperationLog v)
      throws java.sql.SQLException {
    JdbcValues.nullableLong(s, 1, v.userId());
    s.setString(2, v.operationType());
    s.setString(3, v.targetType());
    JdbcValues.nullableLong(s, 4, v.targetId());
    s.setString(5, v.description());
    JdbcValues.time(s, 6, v.createdAt());
  }
}
