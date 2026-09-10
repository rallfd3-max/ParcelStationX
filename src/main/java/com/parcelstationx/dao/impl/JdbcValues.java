package com.parcelstationx.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

final class JdbcValues {
  private JdbcValues() {}

  static LocalDateTime time(ResultSet rows, String column) throws SQLException {
    Timestamp value = rows.getTimestamp(column);
    return value == null ? null : value.toLocalDateTime();
  }

  static Long nullableLong(ResultSet rows, String column) throws SQLException {
    long value = rows.getLong(column);
    return rows.wasNull() ? null : value;
  }

  static void time(PreparedStatement statement, int index, LocalDateTime value)
      throws SQLException {
    statement.setTimestamp(index, value == null ? null : Timestamp.valueOf(value));
  }

  static void nullableLong(PreparedStatement statement, int index, Long value) throws SQLException {
    if (value == null) {
      statement.setNull(index, java.sql.Types.BIGINT);
    } else {
      statement.setLong(index, value);
    }
  }
}
