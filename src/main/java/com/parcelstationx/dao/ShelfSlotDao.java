package com.parcelstationx.dao;

import com.parcelstationx.model.ShelfSlot;
import java.sql.Connection;
import java.util.List;

public interface ShelfSlotDao extends BaseDao<ShelfSlot, Long> {
  List<ShelfSlot> findByShelfId(long shelfId);

  ShelfSlot findByIdForUpdate(Connection connection, long id);
}
