package com.parcelstationx.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
interface RowMapper<T> {
  T map(ResultSet resultSet) throws SQLException;
}
