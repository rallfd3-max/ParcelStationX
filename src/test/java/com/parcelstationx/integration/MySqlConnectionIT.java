package com.parcelstationx.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    try (var connection = DriverManager.getConnection(url, user, password);
        var count =
            connection.prepareStatement(
                "SELECT (SELECT COUNT(*) FROM users),(SELECT COUNT(*) FROM customers),(SELECT COUNT(*) FROM parcels),(SELECT COUNT(*) FROM exception_records),(SELECT COUNT(*) FROM operation_logs)")) {
      try (var rows = count.executeQuery()) {
        rows.next();
        assertTrue(rows.getInt(1) >= 3);
        assertTrue(rows.getInt(2) >= 20);
        assertTrue(rows.getInt(3) >= 50);
        assertTrue(rows.getInt(4) >= 10);
        assertTrue(rows.getInt(5) >= 30);
      }
      try (var rows =
          connection
              .createStatement()
              .executeQuery(
                  "SELECT s.id,s.occupied,COUNT(p.id) FROM shelves s LEFT JOIN parcels p ON p.shelf_id=s.id AND p.status IN ('IN_STOCK','EXCEPTION') GROUP BY s.id,s.occupied ORDER BY s.id")) {
        int[] expected = {17, 17, 16};
        int index = 0;
        while (rows.next() && index < 3) {
          assertEquals(expected[index], rows.getInt(2));
          assertEquals(expected[index], rows.getInt(3));
          index++;
        }
        assertEquals(3, index);
      }
      try (var rows =
          connection
              .createStatement()
              .executeQuery(
                  "SELECT COUNT(*) FROM information_schema.table_constraints WHERE constraint_schema=DATABASE() AND constraint_type IN ('UNIQUE','FOREIGN KEY','CHECK')")) {
        rows.next();
        assertTrue(rows.getInt(1) > 0);
      }
    }
  }
}
