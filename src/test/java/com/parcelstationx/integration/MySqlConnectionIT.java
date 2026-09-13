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
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name IN (?,?,?,?,?,?,?,?,?,?,?)")) {
      String[] tables = {
        "users",
        "customers",
        "shelves",
        "parcels",
        "parcel_events",
        "exception_records",
        "notification_records",
        "operation_logs",
        "shelf_layout",
        "shelf_slots",
        "parcel_relocations"
      };
      for (int i = 0; i < tables.length; i++) statement.setString(i + 1, tables[i]);
      try (var rows = statement.executeQuery()) {
        rows.next();
        assertTrue(rows.getInt(1) == 11, "V2 schema/migration has not created all tables");
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
                  "SELECT s.id,s.occupied,COUNT(p.id) FROM shelves s LEFT JOIN shelf_slots ss ON ss.shelf_id=s.id LEFT JOIN parcels p ON p.slot_id=ss.id AND p.status IN ('IN_STOCK','EXCEPTION') GROUP BY s.id,s.occupied ORDER BY s.id")) {
        int index = 0;
        while (rows.next()) {
          assertEquals(rows.getInt(3), rows.getInt(2));
          index++;
        }
        assertEquals(8, index, "V2.1 migration must expose all eight business shelves");
      }
      try (var rows = connection.createStatement().executeQuery("SELECT COUNT(*) FROM shelf_slots")) {
        rows.next();
        assertTrue(rows.getInt(1) >= 240, "V2.1 migration must expose at least 240 real slots");
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
