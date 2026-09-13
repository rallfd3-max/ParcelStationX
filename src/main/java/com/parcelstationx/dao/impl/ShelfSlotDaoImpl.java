package com.parcelstationx.dao.impl;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.ShelfSlotDao;
import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.ShelfSlot;
import java.sql.Connection;
import java.util.List;

public final class ShelfSlotDaoImpl extends AbstractJdbcDao<ShelfSlot> implements ShelfSlotDao {
  private static final String COLUMNS =
      "id,shelf_id,slot_code,level_index,column_index,enabled,created_at";

  public ShelfSlotDaoImpl(ConnectionProvider connections) {
    super(
        connections,
        "INSERT INTO shelf_slots(shelf_id,slot_code,level_index,column_index,enabled,created_at) VALUES(?,?,?,?,?,?)",
        "UPDATE shelf_slots SET shelf_id=?,slot_code=?,level_index=?,column_index=?,enabled=?,created_at=? WHERE id=?",
        "SELECT " + COLUMNS + " FROM shelf_slots ORDER BY shelf_id,level_index,column_index",
        "SELECT " + COLUMNS + " FROM shelf_slots WHERE id=?",
        "DELETE FROM shelf_slots WHERE id=?",
        ShelfSlotDaoImpl::bind,
        (statement, value) -> {
          bind(statement, value);
          statement.setLong(7, value.id());
        },
        ShelfSlotDaoImpl::map);
  }

  @Override
  public List<ShelfSlot> findByShelfId(long shelfId) {
    return query(
        "SELECT "
            + COLUMNS
            + " FROM shelf_slots WHERE shelf_id=? ORDER BY level_index,column_index",
        (statement, ignored) -> statement.setLong(1, shelfId));
  }

  @Override
  public ShelfSlot findByIdForUpdate(Connection connection, long id) {
    return queryOne(
            connection,
            "SELECT " + COLUMNS + " FROM shelf_slots WHERE id=? FOR UPDATE",
            (statement, ignored) -> statement.setLong(1, id))
        .orElseThrow(() -> new BusinessException("目标仓位不存在。"));
  }

  private static ShelfSlot map(java.sql.ResultSet result) throws java.sql.SQLException {
    return new ShelfSlot(
        result.getLong("id"),
        result.getLong("shelf_id"),
        result.getString("slot_code"),
        result.getInt("level_index"),
        result.getInt("column_index"),
        result.getBoolean("enabled"),
        JdbcValues.time(result, "created_at"));
  }

  private static void bind(java.sql.PreparedStatement statement, ShelfSlot value)
      throws java.sql.SQLException {
    statement.setLong(1, value.shelfId());
    statement.setString(2, value.slotCode());
    statement.setInt(3, value.levelIndex());
    statement.setInt(4, value.columnIndex());
    statement.setBoolean(5, value.enabled());
    JdbcValues.time(statement, 6, value.createdAt());
  }

  @Override
  protected Long idOf(ShelfSlot entity) {
    return entity.id();
  }

  @Override
  protected ShelfSlot withId(ShelfSlot entity, long id) {
    return new ShelfSlot(
        id,
        entity.shelfId(),
        entity.slotCode(),
        entity.levelIndex(),
        entity.columnIndex(),
        entity.enabled(),
        entity.createdAt());
  }
}
