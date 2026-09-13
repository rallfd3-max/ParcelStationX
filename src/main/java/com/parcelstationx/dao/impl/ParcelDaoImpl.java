package com.parcelstationx.dao.impl;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.ParcelDao;
import com.parcelstationx.model.Parcel;
import com.parcelstationx.model.ParcelStatus;
import java.util.Optional;

public final class ParcelDaoImpl extends AbstractJdbcDao<Parcel> implements ParcelDao {
  private static final String COLUMNS =
      "id,tracking_no,courier_company,customer_id,shelf_id,pickup_code,status,arrived_at,picked_up_at,operator_id,remark,created_at,updated_at,slot_id,version";

  public ParcelDaoImpl(ConnectionProvider c) {
    super(
        c,
        "INSERT INTO parcels(tracking_no,courier_company,customer_id,shelf_id,pickup_code,status,arrived_at,picked_up_at,operator_id,remark,created_at,updated_at,slot_id,version) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
        "UPDATE parcels SET tracking_no=?,courier_company=?,customer_id=?,shelf_id=?,pickup_code=?,status=?,arrived_at=?,picked_up_at=?,operator_id=?,remark=?,created_at=?,updated_at=?,slot_id=?,version=? WHERE id=?",
        "SELECT " + COLUMNS + " FROM parcels ORDER BY id",
        "SELECT " + COLUMNS + " FROM parcels WHERE id=?",
        "DELETE FROM parcels WHERE id=?",
        ParcelDaoImpl::bind,
        (s, v) -> {
          bind(s, v);
          s.setLong(15, v.id());
        },
        ParcelDaoImpl::map);
  }

  @Override
  public Optional<Parcel> findByTrackingNo(String value) {
    return queryOne(
        "SELECT " + COLUMNS + " FROM parcels WHERE tracking_no=?", (s, v) -> s.setString(1, value));
  }

  public Optional<Parcel> findByTrackingNo(java.sql.Connection connection, String value) {
    return queryOne(
        connection,
        "SELECT " + COLUMNS + " FROM parcels WHERE tracking_no=?",
        (s, v) -> s.setString(1, value));
  }

  @Override
  public Optional<Parcel> findByPickupCode(String value) {
    return queryOne(
        "SELECT " + COLUMNS + " FROM parcels WHERE pickup_code=? AND status='IN_STOCK'",
        (s, v) -> s.setString(1, value));
  }

  public Optional<Parcel> findByPickupCode(java.sql.Connection connection, String value) {
    return queryOne(
        connection,
        "SELECT " + COLUMNS + " FROM parcels WHERE pickup_code=?",
        (s, v) -> s.setString(1, value));
  }

  @Override
  public Optional<Parcel> findBySlotId(java.sql.Connection connection, long slotId) {
    return queryOne(
        connection,
        "SELECT "
            + COLUMNS
            + " FROM parcels WHERE slot_id=? AND status IN ('IN_STOCK','EXCEPTION')",
        (s, v) -> s.setLong(1, slotId));
  }

  public Optional<Parcel> findByIdForUpdate(java.sql.Connection connection, long id) {
    return queryOne(
        connection,
        "SELECT " + COLUMNS + " FROM parcels WHERE id=? FOR UPDATE",
        (s, v) -> s.setLong(1, id));
  }

  @Override
  public boolean assignSlot(
      java.sql.Connection connection, long parcelId, Long shelfId, Long slotId, long version) {
    try (var statement =
        connection.prepareStatement(
            "UPDATE parcels SET shelf_id=?,slot_id=?,version=version+1,updated_at=CURRENT_TIMESTAMP WHERE id=? AND version=?")) {
      JdbcValues.nullableLong(statement, 1, shelfId);
      JdbcValues.nullableLong(statement, 2, slotId);
      statement.setLong(3, parcelId);
      statement.setLong(4, version);
      return statement.executeUpdate() == 1;
    } catch (java.sql.SQLException exception) {
      throw new com.parcelstationx.exception.DatabaseException(
          "Parcel slot update failed.", exception);
    }
  }

  @Override
  protected Long idOf(Parcel v) {
    return v.id();
  }

  @Override
  protected Parcel withId(Parcel v, long id) {
    return new Parcel(
        id,
        v.trackingNo(),
        v.courierCompany(),
        v.customerId(),
        v.shelfId(),
        v.pickupCode(),
        v.status(),
        v.arrivedAt(),
        v.pickedUpAt(),
        v.operatorId(),
        v.remark(),
        v.createdAt(),
        v.updatedAt(),
        v.slotId(),
        v.version());
  }

  private static Parcel map(java.sql.ResultSet r) throws java.sql.SQLException {
    return new Parcel(
        r.getLong("id"),
        r.getString("tracking_no"),
        r.getString("courier_company"),
        r.getLong("customer_id"),
        JdbcValues.nullableLong(r, "shelf_id"),
        r.getString("pickup_code"),
        ParcelStatus.valueOf(r.getString("status")),
        JdbcValues.time(r, "arrived_at"),
        JdbcValues.time(r, "picked_up_at"),
        r.getLong("operator_id"),
        r.getString("remark"),
        JdbcValues.time(r, "created_at"),
        JdbcValues.time(r, "updated_at"),
        JdbcValues.nullableLong(r, "slot_id"),
        r.getLong("version"));
  }

  private static void bind(java.sql.PreparedStatement s, Parcel v) throws java.sql.SQLException {
    s.setString(1, v.trackingNo());
    s.setString(2, v.courierCompany());
    s.setLong(3, v.customerId());
    JdbcValues.nullableLong(s, 4, v.shelfId());
    s.setString(5, v.pickupCode());
    s.setString(6, v.status().name());
    JdbcValues.time(s, 7, v.arrivedAt());
    JdbcValues.time(s, 8, v.pickedUpAt());
    s.setLong(9, v.operatorId());
    s.setString(10, v.remark());
    JdbcValues.time(s, 11, v.createdAt());
    JdbcValues.time(s, 12, v.updatedAt());
    JdbcValues.nullableLong(s, 13, v.slotId());
    s.setLong(14, v.version());
  }
}
