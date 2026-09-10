package com.parcelstationx.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.DriverManager;
import org.junit.jupiter.api.Test;

class MySqlConnectionIT {
  @Test
  void connectsAndFindsRequiredTables() throws Exception {
    String url = System.getenv("PARCEL_DB_URL"),
        user = System.getenv("PARCEL_DB_USERNAME"),
        password = System.getenv("PARCEL_DB_PASSWORD");
    assumeTrue(url != null && user != null && password != null, "PARCEL_DB_* is not configured");
    try (var connection = DriverManager.getConnection(url, user, password);
        var statement =
            connection.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name IN (?,?,?,?,?,?,?,?)")) {
      String[] tables = {
        "users",
        "customers",
        "shelves",
        "parcels",
        "parcel_events",
        "exception_records",
        "notification_records",
        "operation_logs"
      };
      for (int i = 0; i < tables.length; i++) statement.setString(i + 1, tables[i]);
      try (var rows = statement.executeQuery()) {
        rows.next();
        assertTrue(rows.getInt(1) == 8, "schema.sql has not created all tables");
      }
    }
  }
}
