package com.parcelstationx.dao.impl;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.ParcelEventDao;
import com.parcelstationx.model.*;
import java.util.List;

public final class ParcelEventDaoImpl extends AbstractJdbcDao<ParcelEvent>
    implements ParcelEventDao {
  private static final String C =
      "id,parcel_id,event_type,from_status,to_status,operator_id,description,created_at";

  public ParcelEventDaoImpl(ConnectionProvider p) {
    super(
        p,
        "INSERT INTO parcel_events(parcel_id,event_type,from_status,to_status,operator_id,description,created_at) VALUES(?,?,?,?,?,?,?)",
        "UPDATE parcel_events SET parcel_id=?,event_type=?,from_status=?,to_status=?,operator_id=?,description=?,created_at=? WHERE id=?",
        "SELECT " + C + " FROM parcel_events ORDER BY id",
        "SELECT " + C + " FROM parcel_events WHERE id=?",
        "DELETE FROM parcel_events WHERE id=?",
        ParcelEventDaoImpl::bind,
        (s, v) -> {
          bind(s, v);
          s.setLong(8, v.id());
        },
        ParcelEventDaoImpl::map);
  }

  @Override
  public List<ParcelEvent> findByParcelId(long id) {
    return query(
        "SELECT " + C + " FROM parcel_events WHERE parcel_id=? ORDER BY id",
        (s, v) -> s.setLong(1, id));
  }

  @Override
  protected Long idOf(ParcelEvent v) {
    return v.id();
  }

  @Override
  protected ParcelEvent withId(ParcelEvent v, long id) {
    return new ParcelEvent(
        id,
        v.parcelId(),
        v.eventType(),
        v.fromStatus(),
        v.toStatus(),
        v.operatorId(),
        v.description(),
        v.createdAt());
  }

  private static ParcelEvent map(java.sql.ResultSet r) throws java.sql.SQLException {
    String f = r.getString("from_status"), t = r.getString("to_status");
    return new ParcelEvent(
        r.getLong("id"),
        r.getLong("parcel_id"),
        r.getString("event_type"),
        f == null ? null : ParcelStatus.valueOf(f),
        t == null ? null : ParcelStatus.valueOf(t),
        JdbcValues.nullableLong(r, "operator_id"),
        r.getString("description"),
        JdbcValues.time(r, "created_at"));
  }

  private static void bind(java.sql.PreparedStatement s, ParcelEvent v)
      throws java.sql.SQLException {
    s.setLong(1, v.parcelId());
    s.setString(2, v.eventType());
    s.setString(3, v.fromStatus() == null ? null : v.fromStatus().name());
    s.setString(4, v.toStatus() == null ? null : v.toStatus().name());
    JdbcValues.nullableLong(s, 5, v.operatorId());
    s.setString(6, v.description());
    JdbcValues.time(s, 7, v.createdAt());
  }
}
