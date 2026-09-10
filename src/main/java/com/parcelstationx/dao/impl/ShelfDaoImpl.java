package com.parcelstationx.dao.impl;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.ShelfDao;
import com.parcelstationx.model.Shelf;
import com.parcelstationx.model.ShelfStatus;
import java.util.Optional;

public final class ShelfDaoImpl extends AbstractJdbcDao<Shelf> implements ShelfDao {
  private static final String COLUMNS =
      "id,shelf_code,zone_name,capacity,occupied,status,created_at";

  public ShelfDaoImpl(ConnectionProvider connections) {
    super(
        connections,
        "INSERT INTO shelves(shelf_code,zone_name,capacity,occupied,status,created_at) VALUES(?,?,?,?,?,?)",
        "UPDATE shelves SET shelf_code=?,zone_name=?,capacity=?,occupied=?,status=?,created_at=? WHERE id=?",
        "SELECT " + COLUMNS + " FROM shelves ORDER BY id",
        "SELECT " + COLUMNS + " FROM shelves WHERE id=?",
        "DELETE FROM shelves WHERE id=?",
        ShelfDaoImpl::bind,
        (s, v) -> {
          bind(s, v);
          s.setLong(7, v.id());
        },
        r ->
            new Shelf(
                r.getLong("id"),
                r.getString("shelf_code"),
                r.getString("zone_name"),
                r.getInt("capacity"),
                r.getInt("occupied"),
                ShelfStatus.valueOf(r.getString("status")),
                JdbcValues.time(r, "created_at")));
  }

  @Override
  public Optional<Shelf> findAvailable() {
    return queryOne(
        "SELECT "
            + COLUMNS
            + " FROM shelves WHERE status='ACTIVE' AND occupied<capacity ORDER BY occupied/capacity,id LIMIT 1",
        (s, v) -> {});
  }

  @Override
  protected Long idOf(Shelf v) {
    return v.id();
  }

  @Override
  protected Shelf withId(Shelf v, long id) {
    return new Shelf(
        id, v.shelfCode(), v.zone(), v.capacity(), v.occupied(), v.status(), v.createdAt());
  }

  private static void bind(java.sql.PreparedStatement s, Shelf v) throws java.sql.SQLException {
    s.setString(1, v.shelfCode());
    s.setString(2, v.zone());
    s.setInt(3, v.capacity());
    s.setInt(4, v.occupied());
    s.setString(5, v.status().name());
    JdbcValues.time(s, 6, v.createdAt());
  }
}
