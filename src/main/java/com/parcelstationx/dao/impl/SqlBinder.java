package com.parcelstationx.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
interface SqlBinder<T> {
  void bind(PreparedStatement statement, T value) throws SQLException;
}
