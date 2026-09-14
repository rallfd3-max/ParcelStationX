package com.parcelstationx.dao.impl;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.ShelfLayoutDao;
import com.parcelstationx.model.ShelfLayout;

public final class ShelfLayoutDaoImpl extends AbstractJdbcDao<ShelfLayout>
    implements ShelfLayoutDao {
  private static final String COLUMNS =
      "shelf_id,position_x,position_y,position_z,rotation_y,width,height,depth,columns_count,levels_count,updated_at";

  public ShelfLayoutDaoImpl(ConnectionProvider connections) {
    super(
        connections,
        "INSERT INTO shelf_layout(shelf_id,position_x,position_y,position_z,rotation_y,width,height,depth,columns_count,levels_count,updated_at) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
        "UPDATE shelf_layout SET position_x=?,position_y=?,position_z=?,rotation_y=?,width=?,height=?,depth=?,columns_count=?,levels_count=?,updated_at=? WHERE shelf_id=?",
        "SELECT " + COLUMNS + " FROM shelf_layout ORDER BY shelf_id",
        "SELECT " + COLUMNS + " FROM shelf_layout WHERE shelf_id=?",
        "DELETE FROM shelf_layout WHERE shelf_id=?",
        ShelfLayoutDaoImpl::bindInsert,
        (statement, value) -> {
          statement.setDouble(1, value.positionX());
          statement.setDouble(2, value.positionY());
          statement.setDouble(3, value.positionZ());
          statement.setDouble(4, value.rotationY());
          statement.setDouble(5, value.width());
          statement.setDouble(6, value.height());
          statement.setDouble(7, value.depth());
          statement.setInt(8, value.columns());
          statement.setInt(9, value.levels());
          JdbcValues.time(statement, 10, value.updatedAt());
          statement.setLong(11, value.shelfId());
        },
        result ->
            new ShelfLayout(
                result.getLong("shelf_id"),
                result.getDouble("position_x"),
                result.getDouble("position_y"),
                result.getDouble("position_z"),
                result.getDouble("rotation_y"),
                result.getDouble("width"),
                result.getDouble("height"),
                result.getDouble("depth"),
                result.getInt("columns_count"),
                result.getInt("levels_count"),
                JdbcValues.time(result, "updated_at")));
  }

  @Override
  public ShelfLayout save(ShelfLayout value) {
    String sql =
        "INSERT INTO shelf_layout(shelf_id,position_x,position_y,position_z,rotation_y,width,height,depth,columns_count,levels_count,updated_at) VALUES(?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE position_x=VALUES(position_x),position_y=VALUES(position_y),position_z=VALUES(position_z),rotation_y=VALUES(rotation_y),width=VALUES(width),height=VALUES(height),depth=VALUES(depth),columns_count=VALUES(columns_count),levels_count=VALUES(levels_count),updated_at=VALUES(updated_at)";
    try (var connection = connections.getConnection();
        var statement = connection.prepareStatement(sql)) {
      bindInsert(statement, value);
      statement.executeUpdate();
      return value;
    } catch (java.sql.SQLException exception) {
      throw new com.parcelstationx.exception.DatabaseException(
          "ShelfLayoutDaoImpl failed to save.", exception);
    }
  }

  @Override
  public ShelfLayout save(java.sql.Connection connection, ShelfLayout value) {
    String sql =
        "INSERT INTO shelf_layout(shelf_id,position_x,position_y,position_z,rotation_y,width,height,depth,columns_count,levels_count,updated_at) VALUES(?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE position_x=VALUES(position_x),position_y=VALUES(position_y),position_z=VALUES(position_z),rotation_y=VALUES(rotation_y),width=VALUES(width),height=VALUES(height),depth=VALUES(depth),columns_count=VALUES(columns_count),levels_count=VALUES(levels_count),updated_at=VALUES(updated_at)";
    try (var statement = connection.prepareStatement(sql)) {
      bindInsert(statement, value);
      if (statement.executeUpdate() < 1) {
        throw new com.parcelstationx.exception.DatabaseException(
            "Expected at least one affected layout row.", null);
      }
      return value;
    } catch (java.sql.SQLException exception) {
      throw new com.parcelstationx.exception.DatabaseException(
          "ShelfLayoutDaoImpl failed to save.", exception);
    }
  }

  private static void bindInsert(java.sql.PreparedStatement statement, ShelfLayout value)
      throws java.sql.SQLException {
    statement.setLong(1, value.shelfId());
    statement.setDouble(2, value.positionX());
    statement.setDouble(3, value.positionY());
    statement.setDouble(4, value.positionZ());
    statement.setDouble(5, value.rotationY());
    statement.setDouble(6, value.width());
    statement.setDouble(7, value.height());
    statement.setDouble(8, value.depth());
    statement.setInt(9, value.columns());
    statement.setInt(10, value.levels());
    JdbcValues.time(statement, 11, value.updatedAt());
  }

  @Override
  protected Long idOf(ShelfLayout entity) {
    return entity.shelfId();
  }

  @Override
  protected ShelfLayout withId(ShelfLayout entity, long id) {
    return entity;
  }
}
