package com.parcelstationx.dao.impl;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.ParcelRelocationDao;
import com.parcelstationx.model.ParcelRelocation;
import java.util.List;

public final class ParcelRelocationDaoImpl extends AbstractJdbcDao<ParcelRelocation>
    implements ParcelRelocationDao {
  private static final String COLUMNS =
      "id,parcel_id,from_slot_id,new_slot_id,operator_id,reason,created_at";

  public ParcelRelocationDaoImpl(ConnectionProvider connections) {
    super(
        connections,
        "INSERT INTO parcel_relocations(parcel_id,from_slot_id,new_slot_id,operator_id,reason,created_at) VALUES(?,?,?,?,?,?)",
        "UPDATE parcel_relocations SET parcel_id=?,from_slot_id=?,new_slot_id=?,operator_id=?,reason=?,created_at=? WHERE id=?",
        "SELECT " + COLUMNS + " FROM parcel_relocations ORDER BY id",
        "SELECT " + COLUMNS + " FROM parcel_relocations WHERE id=?",
        "DELETE FROM parcel_relocations WHERE id=?",
        ParcelRelocationDaoImpl::bind,
        (statement, value) -> {
          bind(statement, value);
          statement.setLong(7, value.id());
        },
        ParcelRelocationDaoImpl::map);
  }

  public List<ParcelRelocation> findByParcelId(long parcelId) {
    return query(
        "SELECT " + COLUMNS + " FROM parcel_relocations WHERE parcel_id=? ORDER BY created_at,id",
        (statement, ignored) -> statement.setLong(1, parcelId));
  }

  private static void bind(java.sql.PreparedStatement statement, ParcelRelocation value)
      throws java.sql.SQLException {
    statement.setLong(1, value.parcelId());
    JdbcValues.nullableLong(statement, 2, value.fromSlotId());
    JdbcValues.nullableLong(statement, 3, value.newSlotId());
    statement.setLong(4, value.operatorId());
    statement.setString(5, value.reason());
    JdbcValues.time(statement, 6, value.createdAt());
  }

  private static ParcelRelocation map(java.sql.ResultSet result) throws java.sql.SQLException {
    return new ParcelRelocation(
        result.getLong("id"),
        result.getLong("parcel_id"),
        JdbcValues.nullableLong(result, "from_slot_id"),
        JdbcValues.nullableLong(result, "new_slot_id"),
        result.getLong("operator_id"),
        result.getString("reason"),
        JdbcValues.time(result, "created_at"));
  }

  @Override
  protected Long idOf(ParcelRelocation entity) {
    return entity.id();
  }

  @Override
  protected ParcelRelocation withId(ParcelRelocation entity, long id) {
    return new ParcelRelocation(
        id,
        entity.parcelId(),
        entity.fromSlotId(),
        entity.newSlotId(),
        entity.operatorId(),
        entity.reason(),
        entity.createdAt());
  }
}
